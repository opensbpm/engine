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
package org.opensbpm.engine.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.events.EngineEvent;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.events.ProcessInstanceChangedEvent;
import org.opensbpm.engine.api.events.ProcessModelChangedEvent;
import org.opensbpm.engine.api.events.ProviderTaskChangedEvent;
import org.opensbpm.engine.api.events.RoleChangedEvent;
import org.opensbpm.engine.api.events.RoleUserChangedEvent;
import org.opensbpm.engine.api.events.UserChangedEvent;
import org.opensbpm.engine.api.events.UserProcessInstanceChangedEvent;
import org.opensbpm.engine.api.events.UserProcessModelChangedEvent;
import org.opensbpm.engine.api.events.UserTaskChangedEvent;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.RoleToken;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.core.engine.EngineConverter;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ServiceSubject;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.SubjectVisitor;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.ModelConverter;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.opensbpm.engine.core.engine.EngineConverter.convertUser;
import static org.opensbpm.engine.core.model.ModelConverter.convertModel;
import static org.opensbpm.engine.core.model.entities.StateVisitor.functionState;
import static org.opensbpm.engine.utils.StreamUtils.flatMapToList;
import static org.opensbpm.engine.utils.StreamUtils.mapToList;

@Component
public class EngineEventPublisher {

    private static final Logger LOGGER = Logger.getLogger(EngineEventPublisher.class.getName());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ProcessModelService processModelService;

    @Autowired
    private EngineConverter engineConverter;

    public void fireRoleChanged(Role role, Type type) {
        publishEvents(new RoleChangedEvent(RoleToken.of(role.getId(), role.getName()), type));
    }

    public void fireRoleUserChanged(Role role, User user) {
        publishEvents(createUserEvents(role, user, Type.DELETE));
    }

    public void publishUserEvents(Role role, Collection<User> users, Type type) {
        List<EngineEvent<?>> events = flatMapToList(users, user
                -> createUserEvents(role, user, type).stream());
        publishEvents(events);
    }

    private List<EngineEvent<?>> createUserEvents(Role role, User user, Type type) {
        List<EngineEvent<?>> engineEvent = new ArrayList<>();
        engineEvent.add(new RoleUserChangedEvent(role.getId(), convertUser(user), type));

        List<UserProcessModelChangedEvent> changeEvents = mapToList(processModelService.findAllStartableByRole(role),
                processModel -> new UserProcessModelChangedEvent(user.getId(), convertModel(processModel), type)
        );
        engineEvent.addAll(changeEvents);
        return engineEvent;
    }

    public void fireUserChanged(User entity, Type type) {
        publishEvents(new UserChangedEvent(convertUser(entity), type));
    }

    public void fireProcessModelChanged(ProcessModel savedModel, Type type) {
        publishModelEvent(savedModel, type);
        publishUserEvents(savedModel, type);
    }

    public void fireProcessModelUpdate(ProcessModel processModel) {
        publishModelEvent(processModel, Type.UPDATE);
        if (ProcessModelState.ACTIVE == processModel.getState()) {
            publishUserEvents(processModel, Type.CREATE);
        } else if (ProcessModelState.INACTIVE == processModel.getState()) {
            publishUserEvents(processModel, Type.DELETE);
        }
    }

    private void publishModelEvent(ProcessModel processModel, Type type) {
        publishEvents(new ProcessModelChangedEvent(toModelInfo(processModel), type));
    }

    private void publishUserEvents(ProcessModel processModel, Type type) {
        publishEvents(createUserEvents(processModel, type));
    }

    private List<UserProcessModelChangedEvent> createUserEvents(ProcessModel processModel, Type type) {
        return processModel.getUserSubjectModels().stream()
                .filter(SubjectModel::isStarter)
                .flatMap(UserSubjectModel::getAllUsers)
                .map(user -> new UserProcessModelChangedEvent(user.getId(), toModelInfo(processModel), type))
                .collect(Collectors.toList());
    }

    private ProcessModelInfo toModelInfo(ProcessModel processModel) {
        return ModelConverter.convertModel(processModel);
    }

    public void fireProcessInstanceChanged(ProcessInstance processInstance, Type type) {
        Collection<EngineEvent<?>> engineEvents = new ArrayList<>();
        ProcessInfo processInfo = engineConverter.convertInstance(processInstance);
        engineEvents.add(new ProcessInstanceChangedEvent(processInfo, type));
        if (processInstance.isStopped()) {
            processInstance.getUserSubjects().forEach(subject
                    -> subject.getCurrentOrAllUsers()
                            .forEach(user -> {
                                engineEvents.add(new UserProcessInstanceChangedEvent(user.getId(), processInfo, Type.DELETE));
                                subject.getCurrent(functionState()).ifPresent(state
                                        -> {
                                    TaskInfo taskInfo = engineConverter.convertSubjectState(subject, state);
                                    engineEvents.add(new UserTaskChangedEvent(user.getId(), taskInfo, Type.DELETE));
                                }
                                );
                            }));
        }
        publishEvents(engineEvents);
    }

    public void fireSubjectStateChanged(Subject subject, FunctionState functionState, Type type) {
        List<EngineEvent<?>> events = subject.accept(new SubjectVisitor<List<EngineEvent<?>>>() {
            @Override
            public List<EngineEvent<?>> visitServiceSubject(ServiceSubject serviceSubject) {
                if (functionState.getProviderName() == null) {
                    //don't know how to execute function without provider
                    throw new IllegalStateException("No provider for " + functionState.getName());
                }
                TaskInfo taskInfo = engineConverter.convertSubjectState(serviceSubject, functionState);
                return Collections.<EngineEvent<?>>singletonList(new ProviderTaskChangedEvent(taskInfo, type));
            }

            @Override
            public List<EngineEvent<?>> visitUserSubject(UserSubject userSubject) {
                return mapToList(userSubject.getCurrentOrAllUsers(), user -> createUserTaskChangedEvent(user));
            }

            private UserTaskChangedEvent createUserTaskChangedEvent(User user) {
                TaskInfo taskInfo = engineConverter.convertSubjectState(subject, functionState);
                return new UserTaskChangedEvent(user.getId(), taskInfo, type);
            }

        });
        publishEvents(events);
    }

    private void publishEvents(EngineEvent<?>... engineEvents) {
        publishEvents(Arrays.asList(engineEvents));
    }

    private void publishEvents(Collection<? extends EngineEvent<?>> engineEvents) {
        publishEvents(() -> engineEvents);
    }

    private void publishEvents(Supplier<Collection<? extends EngineEvent<?>>> engineEvents) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                engineEvents.get().forEach(engineEvent -> {
                    LOGGER.log(Level.FINE, "publish {0}", engineEvent);
                    publisher.publishEvent(engineEvent);
                });
            }
        });
    }

}
