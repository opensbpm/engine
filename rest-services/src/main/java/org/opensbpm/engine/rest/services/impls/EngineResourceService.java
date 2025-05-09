/**
 * ****************************************************************************
 * Copyright (C) 2024 Stefan Sedelmaier
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package org.opensbpm.engine.rest.services.impls;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response.Status;
import org.opensbpm.engine.api.*;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskNotFoundException;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.ProcessModelInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensbpm.engine.rest.api.EngineResource;
import org.opensbpm.engine.rest.api.dto.instance.Processes;
import org.opensbpm.engine.rest.api.dto.instance.Tasks;
import org.opensbpm.engine.rest.api.dto.model.ProcessModels;
import org.opensbpm.engine.rest.services.authentication.SpringAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static org.opensbpm.engine.utils.StreamUtils.filterToOne;

@Component
public class EngineResourceService implements EngineResource {

    private final EngineService engineService;
    private final UserTokenService userTokenService;

    public EngineResourceService(EngineService engineService, UserTokenService userTokenService) {
        this.engineService = Objects.requireNonNull(engineService, "EngineService must not be null");
        this.userTokenService = Objects.requireNonNull(userTokenService, "UserTokenService must not be null");
    }

    @Override
    public ProcessModelResource getProcessModelResource(Long userId) {
        UserToken userToken = validateUser(userId);
        return new ProcessModelResourceService(userToken);
    }

    @Override
    public ProcessInstanceResource getProcessInstanceResource(Long userId) {
        UserToken userToken = validateUser(userId);
        return new ProcessInstanceResourceService(userToken);
    }

    @Override
    public TaskResource getTaskResource(Long userId) {
        UserToken userToken = validateUser(userId);
        return new TaskResourceService(userToken);
    }

    private UserToken validateUser(Long userId) {
        UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
        if (userId.compareTo(userToken.getId()) != 0) {
            throw new ClientErrorException("User mismatch", Status.FORBIDDEN);
        }
        return userToken;
    }

    private UserToken retrieveToken(Authentication authentication) {
        try {
            return userTokenService.retrieveToken(SpringAuthentication.of(authentication));
        } catch (UserNotFoundException ex) {
            throw new ClientErrorException(ex.getMessage(), Status.FORBIDDEN);
        }
    }

    public class ProcessModelResourceService implements ProcessModelResource {

        private final UserToken userToken;

        public ProcessModelResourceService(UserToken userToken) {
            this.userToken = userToken;
        }

        @Override
        public ProcessModels index() {
            try {
                return ProcessModels.of(findProcessModels(userToken));
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public ProcessModelInfo retrieve(Long modelId) {
            try {
                return findProcessModels(userToken).stream().filter(modelInfo -> modelId.equals(modelInfo.getId())).findFirst().orElseThrow(() -> new NotFoundException());
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public TaskInfo start(Long modelId) {
            try {
                return engineService.startProcess(userToken, ModelRequest.of(modelId));
            } catch (UserNotFoundException | ModelNotFoundException ex) {
                Logger.getLogger(ProcessModelResourceService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        private Collection<ProcessModelInfo> findProcessModels(UserToken userToken) throws UserNotFoundException {
            return engineService.findStartableProcessModels(userToken);
        }
    }

    public class ProcessInstanceResourceService implements ProcessInstanceResource {
        private final UserToken userToken;

        public ProcessInstanceResourceService(UserToken userToken) {
            this.userToken = userToken;
        }

        @Override
        public Processes index() {
            try {
                return new Processes(new ArrayList<>(findProcessInfos()));
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public ProcessInfo retrieve(Long instanceId) {
            try {
                return findProcessInfos().stream()
                        .filter(process -> process.getId().equals(instanceId))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException());
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        private Collection<ProcessInfo> findProcessInfos() throws UserNotFoundException {
            return engineService.findAllByUserAndState(userToken, ProcessInstanceState.ACTIVE);
        }

    }

    public class TaskResourceService implements TaskResource {

        private final UserToken userToken;

        public TaskResourceService(UserToken userToken) {
            this.userToken = userToken;
        }

        @Override
        public Tasks index() {
            try {
                return new Tasks(engineService.getTasks(userToken));
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public TaskResponse retrieve(Long taskId) {
            try {
                //TODO add taskId to QUERY (if possible)
                TaskInfo taskInfo = filterToOne(engineService.getTasks(userToken),
                        task -> Objects.equals(task.getId(), taskId))
                        .orElseThrow(() -> new ClientErrorException("Task with id " + taskId + " doesn't exists anymore", Status.GONE));
                return engineService.getTaskResponse(userToken, taskInfo);
            } catch (UserNotFoundException | TaskNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            } catch (TaskOutOfDateException ex) {
                throw new ClientErrorException(ex.getMessage(), Status.GONE, ex);
            }
        }

        @Override
        public void submit(Long taskId, TaskRequest taskRequest) {
            try {
                if (!taskId.equals(taskRequest.getId())) {
                    throw new ClientErrorException("Task id " + taskId + " doesn't match TaskRequest " + taskRequest.getId(), Status.BAD_REQUEST);
                }
                engineService.executeTask(userToken, taskRequest);
            } catch (UserNotFoundException | SubjectAlreadyBoundException | TaskNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            } catch (TaskOutOfDateException ex) {
                throw new ClientErrorException(ex.getMessage(), Status.GONE, ex);
            }
        }

    }

}
