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
 * ****************************************************************************
 */
package org.opensbpm.engine.core;

import org.opensbpm.engine.api.EngineService;
import org.opensbpm.engine.api.ModelNotFoundException;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.UserNotFoundException;
import org.opensbpm.engine.api.instance.AutocompleteResponse;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskNotFoundException;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.core.engine.EngineConverter;
import org.opensbpm.engine.core.engine.ProcessInstanceService;
import org.opensbpm.engine.core.engine.StateChangeService;
import org.opensbpm.engine.core.engine.SubjectService;
import org.opensbpm.engine.core.engine.UserSubjectService;
import org.opensbpm.engine.core.engine.UserService;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.engine.entities.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.opensbpm.engine.core.ExceptionFactory.newModelNotFoundException;
import static org.opensbpm.engine.core.ExceptionFactory.newTaskNotFoundException;
import static org.opensbpm.engine.core.ExceptionFactory.newTaskOutOfDateException;
import static org.opensbpm.engine.core.ExceptionFactory.newUserNotFoundException;

import org.opensbpm.engine.core.engine.ValidationService;

import static org.opensbpm.engine.core.model.ModelConverter.convertModels;

import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import java.text.MessageFormat;

/**
 * Service implementation of {@link EngineService}. All nesercary database
 * transactions for a {@link EngineService} call are started and commited in
 * this class.
 */
@Service
public class EngineServiceBoundary implements EngineService {

    private static final Logger LOGGER = Logger.getLogger(EngineServiceBoundary.class.getName());

    @Autowired
    private ProcessModelService processModelService;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserSubjectService userSubjectService;

    @Autowired
    private StateChangeService stateChangeService;

    @Autowired
    private EngineConverter engineConverter;

    @Autowired
    private ValidationService validationService;

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public Collection<ProcessModelInfo> findStartableProcessModels(UserToken userToken) throws UserNotFoundException {
        User user = getUser(userToken);
        return convertModels(processModelService.findAllStartableByUser(user));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public TaskInfo startProcess(UserToken userToken, ModelRequest modelRequest) throws UserNotFoundException, ModelNotFoundException {
        User startUser = getUser(userToken);
        //TODO validate startable ProcessModels
        ProcessModel processModel = processModelService.findById(modelRequest.getId())
                .orElseThrow(newModelNotFoundException(modelRequest.getId()));

        ProcessInstance processInstance = processInstanceService.start(processModel, startUser);
        Subject subject = subjectService.createSubject(processInstance, processModel.getStarterSubjectModel(), startUser);

        LOGGER.log(Level.INFO, "{0} started as {1}", new Object[]{processModel, processInstance});

        return createTaskInfoFromSubject(subject)
                .orElseThrow(() -> {
                    String msg = MessageFormat.format("no visible current state after start for subject {0}", subject);
                    return new IllegalStateException(msg);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public List<ProcessInfo> findAllByUserAndState(UserToken userToken, ProcessInstanceState state) throws UserNotFoundException {
        User user = getUser(userToken);
        return engineConverter.convertInstances(processInstanceService.findAllByUserAndState(user, state));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public List<TaskInfo> getTasks(UserToken userToken) throws UserNotFoundException {
        User user = getUser(userToken);
        return userSubjectService.findAllByUser(user).stream()
                .map(subject -> createTaskInfoFromSubject(subject))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TaskInfo> createTaskInfoFromSubject(Subject subject) {
        return subject.getVisibleCurrentState()
                .filter(state -> !state.isEnd())
                .map(state -> engineConverter.convertSubjectState(subject, state));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public TaskResponse getTaskResponse(UserToken userToken, TaskInfo taskInfo) throws TaskNotFoundException, TaskOutOfDateException {
        Subject subject = validateTaskInfo(taskInfo);
        return engineConverter.createTaskResponse(subject);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public AutocompleteResponse getAutocompleteResponse(UserToken userToken, TaskInfo taskInfo, ObjectRequest objectRequest, String queryString) throws TaskNotFoundException, TaskOutOfDateException {
        Subject subject = validateTaskInfo(taskInfo);
        FunctionState currentState = subject.getVisibleCurrentState()
                .orElseThrow(() -> {
                    String msg = MessageFormat.format("no current state for {0}", subject.toString());
                    return new IllegalStateException(msg);
                });

        ObjectModel objectModel = subject.getProcessInstance().getProcessModel().getObjectModels(currentState)
                .filter(model -> model.getId().equals(objectRequest.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    String msg = MessageFormat.format("ObjectModel {0} not found in state {1}",
                            objectRequest.getId(), currentState.getName());
                    return new IllegalStateException(msg);
                });
        return validationService.createAutocompleteResponse(currentState, objectModel, queryString);
    }

    private Subject validateTaskInfo(TaskInfo taskInfo) throws TaskNotFoundException, TaskOutOfDateException {
        //TODO activate //User user = getUser(userToken);
        //TODO lock subject and and throw Exception

        //There is no direct database representation for a task; basicly a Subject with a State represents a task
        Subject subject = subjectService.findById(taskInfo.getId())
                .orElseThrow(newTaskNotFoundException(taskInfo.getId()));
        if (subject.getLastChanged().isAfter(taskInfo.getLastChanged())) {
            throw newTaskOutOfDateException(subject, taskInfo.getLastChanged());
        } else if (!subject.getVisibleCurrentState()
                .filter(state -> !state.isEnd())
                .isPresent()) {
            //not sure why this happens, subject should be locked with LockModeType.PESSIMISTIC_WRITE
            throw new TaskOutOfDateException("no state for subject " + subject);
        } else {
            return subject;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Boolean executeTask(UserToken userToken, TaskRequest taskRequest)
            throws UserNotFoundException, TaskNotFoundException, TaskOutOfDateException {
        User user = getUser(userToken);
        UserSubject userSubject = userSubjectService.retrieveForWrite(taskRequest.getId())
                .orElseThrow(newTaskNotFoundException(taskRequest.getId()));
        if (userSubject.getUser() == null) {
            userSubject.setUser(user);
        } else if (!user.equalsId(userSubject.getUser())) {
            throw new IllegalStateException(user + " is not user of subject " + userSubject.getId() + " (current user " + userSubject.getUser());
        }

        if (userSubject.getLastChanged().isAfter(taskRequest.getLastChanged())) {
            //this happens if the task is already changed by the same user
            LOGGER.log(Level.SEVERE, "TaskOutOfDate: userId={0},processId={1} subject:{2} changeObject: {3}", new Object[]{user.getId(), userSubject.getProcessInstance().getId(), userSubject.getLastChanged(), taskRequest.getLastChanged()});
            throw newTaskOutOfDateException(userSubject, taskRequest.getLastChanged());
        }
        return stateChangeService.changeState(userSubject, taskRequest);
    }

    private User getUser(UserToken userToken) throws UserNotFoundException {
        return userService.findById(userToken.getId())
                .orElseThrow(newUserNotFoundException(userToken.getName()));
    }

}
