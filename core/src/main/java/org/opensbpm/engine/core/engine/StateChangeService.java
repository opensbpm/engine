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

@Service
public class StateChangeService {

    private static final Logger LOGGER = Logger.getLogger(StateChangeService.class.getName());

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ObjectInstanceService objectInstanceService;

    public boolean changeState(Subject subject, TaskRequest taskRequest) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(taskRequest);

        FunctionState currentState = subject.getVisibleCurrentState()
                .orElseThrow(() -> new IllegalStateException("current visible state " + subject.getVisibleCurrentState().toString() + " not FunctionState"));

        State nextState = filterToOne(currentState.getHeads(), state
                -> state.getId().equals(taskRequest.getNextState().getId()))
                .orElseThrow(() -> new IllegalArgumentException("State-id " + taskRequest.getNextState() + " not in possible next-states"));
        LOGGER.log(Level.FINE, "change state of {0} from {1}/{2} to {3}", new Object[]{subject, subject.getCurrentState(), currentState, nextState});

        //TODO fix wrong behaviour: when state has mandatory fields and ObjectData is empty no error is thrown
        Optional.ofNullable(taskRequest.getObjectData())
                .ifPresent(datas -> {
                    List<ObjectInstance> objectInstances = updateObjectInstances(subject.getProcessInstance(), currentState, datas);
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

    private List<ObjectInstance> updateObjectInstances(ProcessInstance processInstance, FunctionState state, List<ObjectData> objectDatas) {
        return processInstance.getProcessModel().getObjectModels().stream()
                .filter(objectModel -> state.hasAnyStatePermission(objectModel))
                .flatMap(objectModel
                        -> objectDatas.stream()
                        .filter(objectData -> objectModel.getName().equals(objectData.getName()))
                        .map(objectData -> updateObjectInstance(processInstance, state, objectModel, objectData)))
                .collect(Collectors.toList());
    }

    private ObjectInstance updateObjectInstance(ProcessInstance processInstance, FunctionState state, ObjectModel objectModel, ObjectData objectData) {
        ObjectSchema objectSchema = ObjectSchemaConverter.toObjectSchema(state, objectModel);
        
        ObjectInstance objectInstance = processInstance.getOrAddObjectInstance(objectModel);

        AttributeStore attributeStore = new AttributeStore(objectSchema, new HashMap<>(objectInstance.getValue()));
        attributeStore.updateValues(objectData.getData());
        objectInstance.setValue(attributeStore.toIdMap(attribute -> true));
        return objectInstance;
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

            Optional<Subject> sender = subject.getProcessInstance().findActiveSubject(message.getSender());
            if (sender.isPresent()) {
                Subject senderSubject = sender.get();
                //switch sender send-state to next state
                LOGGER.log(Level.FINE, "received Message from {0}", sender);

                senderSubject.getCurrent(sendState()).ifPresent(sendState -> {
                    if (sendState.getHead() != null) {
                        switchToNextState(senderSubject, sendState.getHead());
                    }
                });
            } else {
                Object[] params = new Object[]{subject.getProcessInstance(), message.getSender()};
                LOGGER.log(Level.FINER, "no active sender-subject for {0} {1}", params);
            }
        });
    }

}
