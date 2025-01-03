/** *****************************************************************************
 * Copyright (C) 2020 Stefan Sedelmaier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************
 */
package org.opensbpm.engine.core.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.AttributeStore;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.core.engine.ScriptExecutorService.BindingContext;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static org.opensbpm.engine.core.model.entities.StateVisitor.receiveState;
import static org.opensbpm.engine.core.model.entities.StateVisitor.sendState;
import static org.opensbpm.engine.utils.StreamUtils.filterToOne;
import static org.opensbpm.engine.utils.StreamUtils.toOne;

@Service
public class StateChangeService {

    private static final Logger LOGGER = Logger.getLogger(StateChangeService.class.getName());

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ObjectInstanceService objectInstanceService;

    @Autowired
    private ScriptExecutorService scriptService;

    public boolean changeState(Subject subject, TaskRequest taskRequest) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(taskRequest);

        FunctionState currentState = subject.getVisibleCurrentState()
                .orElseThrow(() -> new IllegalStateException("current visible state " + subject.getVisibleCurrentState().toString() + " not FunctionState"));

        State nextState = filterToOne(currentState.getHeads(), state
                -> state.getId().equals(taskRequest.getNextState().getId()))
                .orElseThrow(() -> new IllegalArgumentException("State " + taskRequest.getNextState() + " not in possible next-states"));
        LOGGER.log(Level.FINE, "change state of {0} from {1}/{2} to {3}", new Object[]{subject, subject.getCurrentState(), currentState, nextState});

        //TODO fix wrong behaviour: when state has mandatory fields and ObjectData is empty no error is thrown
        Optional.ofNullable(taskRequest.getObjectData())
                .ifPresent(datas -> {
                    List<ObjectInstance> objectInstances = updateObjectInstances(subject, currentState, datas);
                    objectInstanceService.saveAll(objectInstances);
                });

        switchToNextState(subject, nextState);

        ProcessInstance processInstance = subject.getProcessInstance();
        if (!processInstance.hasActiveSubjects()) {
            LOGGER.log(Level.FINE, "finish {0} after all subjects in 'END' state, set process instance to 'FINISHED'", processInstance);
            processInstanceService.finish(processInstance);
        }
        //PENDING validate deadlock (all active-subjects have no visible state)
        return true;
    }

    private List<ObjectInstance> updateObjectInstances(Subject subject, FunctionState state, List<ObjectData> objectDatas) {
        return objectDatas.stream()
                .map(objectData -> {
                    ObjectInstance objectInstance = findByObjectModel(subject.getProcessInstance(), state, objectData);
                    BindingContext bindingContext = BindingContext.ofSubject(subject);

                    ObjectSchema objectSchema = ObjectSchemaConverter.toObjectSchema(scriptService, state, objectInstance.getObjectModel(), bindingContext);

                    AttributeStore attributeStore = new AttributeStore(objectSchema, new HashMap<>(objectInstance.getValue()));
                    attributeStore.updateValues(objectData.getData());
                    objectInstance.setValue(attributeStore.toIdMap(attribute -> true));
                    return objectInstance;
                })
                .collect(Collectors.toList());
    }

    private ObjectInstance findByObjectModel(ProcessInstance processInstance, FunctionState state, ObjectData objectData) {
        return processInstance.getProcessModel().getObjectModels().stream()
                .filter(objectModel -> state.hasAnyStatePermission(objectModel))
                .filter(objectModel -> objectModel.getName().equals(objectData.getName()))
                .reduce(toOne())
                .map(objectModel -> processInstance.getOrAddObjectInstance(objectModel))
                .orElseThrow(() -> new IllegalArgumentException("No ObjectModel for " + objectData + " found"));
    }

    private void switchToNextState(Subject subject, State nextState) {
        LOGGER.log(Level.FINER, "switch {0} to next state {1}", new Object[]{subject, nextState});
        subject.getCurrent(receiveState()).ifPresent(receiveState
                -> receiveMessages(subject, receiveState));

        subjectService.updateState(subject, nextState);

        nextState.accept(sendState()).ifPresent(sendState -> {
            Subject receiver = subjectService.sendMessageToReceiver(subject, sendState);

            if (sendState.isAsync() && sendState.getHead() != null) {
                switchToNextState(subject, sendState.getHead());
            }

            receiver.getVisibleCurrentState()
                    .filter(functionState -> functionState.isEnd())
                    .ifPresent(functionState -> {
                        //special case, next state after receive is end
                        //as in ExampleProcessBookPag103IT#testReject
                        switchToNextState(receiver, functionState);
                    });
        });
    }

    private void receiveMessages(Subject subject, ReceiveState receiveState) {
        LOGGER.log(Level.FINER, "{0} receive messages of {1}", new Object[]{subject, receiveState});
        receiveState.getMessageModels().stream()
                .filter(messageModel -> subject.hasUnconsumedMessages(messageModel.getObjectModel()))
                .forEach(messageModel
                        -> {
                    LOGGER.log(Level.FINER, "{0} consume message {1}", new Object[]{subject, messageModel.getHead()});
                    //PENDING use switchToNextState to support sendState as message-head
                    //but be carefull do not receive messages twice
                    //switchToNextState(subject, messageModel.getHead());
                    subjectService.updateState(subject, messageModel.getHead(), false);

                    consumeMessage(subject, messageModel.getObjectModel());
                });

    }

    private void consumeMessage(Subject subject, ObjectModel objectModel) {
        subject.getUnconsumedMessages(objectModel).forEach(message -> {
            message.setConsumed(true);
            Subject senderSubject = message.getSender();
            //switch sender send-state to next state
            LOGGER.log(Level.FINE, "received Message from {0}", senderSubject);

            senderSubject.getCurrent(sendState()).ifPresent(sendState -> {
                if (sendState.getHead() != null) {
                    switchToNextState(senderSubject, sendState.getHead());
                }
            });
        });
    }

}
