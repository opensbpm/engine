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

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ServiceSubject;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.StateGraph;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.SubjectModelVisitor;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import static org.opensbpm.engine.core.model.entities.StateVisitor.functionState;

@Service
public class SubjectService {

    private static final Logger LOGGER = Logger.getLogger(SubjectService.class.getName());

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    protected EngineEventPublisher eventPublisher;

    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    public Subject createSubject(ProcessInstance processInstance, SubjectModel subjectModel, User user) {
        final Subject subject = subjectModel.accept(new SubjectModelVisitor<Subject>() {
            @Override
            public Subject visitUserSubjectModel(UserSubjectModel userSubjectModel) {
                return new UserSubject(processInstance, userSubjectModel, user);
            }

            @Override
            public Subject visitServiceSubjectModel(ServiceSubjectModel serviceSubjectModel) {
                return new ServiceSubject(processInstance, serviceSubjectModel);
            }

        });

        StateGraph stateGraph = new StateGraph(subjectModel);
        State startState = stateGraph.getRoots().stream()
                .filter(State::isStart)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no start-state for Subject " + subject.getSubjectModel()));
        updateState(subject, startState);

        LOGGER.log(Level.INFO, "subject {0} initialized", subject);
        return subject;
    }

    public void updateState(Subject subject, State state) {
        updateState(subject, state, true);
    }

    public void updateState(Subject subject, State state, boolean fireEvents) {
        Objects.requireNonNull(subject, "subject must be non null");
        Objects.requireNonNull(state, "state must be non null");

        State oldState = subject.getCurrentState();

        if (oldState == null || !state.equalsId(oldState)) {
            LOGGER.log(Level.FINE, "{0} update state from {1} to {2}", new Object[]{subject, oldState, state});
            subject.setCurrentState(state);

            //flush now to generate correct id
            Subject savedSubject = subjectRepository.save(subject);
            if (fireEvents) {
                if (oldState != null) {
                    publishDeleteEvents(savedSubject, oldState);
                }
                publishCreateEvents(savedSubject);
            }
        } else {
            LOGGER.log(Level.FINE, "{0} skip update state", subject);
        }
    }

    public Subject sendMessageToReceiver(Subject sender, SendState sendState) {
        ProcessInstance processInstance = sender.getProcessInstance();
        Subject receiverSubject = processInstance.findActiveSubject(sendState.getReceiver())
                .orElseGet(() -> createSubject(processInstance, sendState.getReceiver(), null));

        receiverSubject.addMessage(sendState.getObjectModel(), sender);

        publishCreateEvents(receiverSubject);
        return subjectRepository.save(receiverSubject);
    }

    private void publishDeleteEvents(Subject subject, State state) {
        state.accept(functionState())
                .ifPresent(functionState -> {
                    eventPublisher.fireSubjectStateChanged(subject, functionState, Type.DELETE);
                });
    }

    private void publishCreateEvents(Subject subject) {
        subject.getVisibleCurrentState()
                .filter(state -> !state.isEnd())
                .ifPresent(functionState -> {
                    eventPublisher.fireSubjectStateChanged(subject, functionState, Type.CREATE);
                });
    }

    @Repository
    public interface SubjectRepository extends JpaSpecificationRepository<Subject, Long> {

    }

}
