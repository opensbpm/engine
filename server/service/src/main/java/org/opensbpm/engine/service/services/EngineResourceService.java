/*******************************************************************************
 * Copyright (C) 2024 Stefan Sedelmaier
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
 ******************************************************************************/
package org.opensbpm.engine.service.services;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response.Status;
import org.opensbpm.authentication.SpringAuthentication;
import org.opensbpm.engine.api.EngineService;
import org.opensbpm.engine.api.ModelNotFoundException;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.UserNotFoundException;
import org.opensbpm.engine.api.UserTokenService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensbpm.engine.server.api.EngineResource;
import org.opensbpm.engine.server.api.dto.instance.Processes;
import org.opensbpm.engine.server.api.dto.instance.Tasks;
import org.opensbpm.engine.server.api.dto.model.ProcessModels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import static org.opensbpm.engine.utils.StreamUtils.filterToOne;

@Component
public class EngineResourceService implements EngineResource {

    @Autowired
    private EngineService engineService;

    @Autowired
    private UserTokenService userTokenService;

    @Override
    public ProcessModelResource getProcessModelResource(Long userId) {
        return new ProcessModelResourceService();
    }

    @Override
    public ProcessInstanceResource getProcessInstanceResource(Long userId) {
        return new ProcessInstanceResourceService();
    }

    @Override
    public TaskResource getTaskResource(Long userId) {
        return new TaskResourceService();
    }

    private UserToken retrieveToken(Authentication authentication) throws UserNotFoundException {
        return userTokenService.retrieveToken(SpringAuthentication.of(authentication));
    }

    public class ProcessModelResourceService implements ProcessModelResource {

        @Override
        public ProcessModels index() {
            try {
                UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
                return ProcessModels.of(findProcessModels(userToken));
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public ProcessModelInfo retrieve(Long modelId) {
            try {
                UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
                return findProcessModels(userToken).stream().filter(modelInfo -> modelId.equals(modelInfo.getId())).findFirst().orElseThrow(() -> new NotFoundException());
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public TaskInfo start(Long modelId) {
            try {
                UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
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
                return findProcessInfos().stream().filter(process -> process.getId().equals(instanceId)).findFirst().orElseThrow(() -> new NotFoundException());
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        private Collection<ProcessInfo> findProcessInfos() throws UserNotFoundException {
            UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
            return engineService.findAllByUserAndState(userToken, ProcessInstanceState.ACTIVE);
        }

    }

    public class TaskResourceService implements TaskResource {

        @Override
        public Tasks index() {
            try {
                UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
                return new Tasks(engineService.getTasks(userToken));
            } catch (UserNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            }
        }

        @Override
        public TaskResponse retrieve(Long taskId) {
            try {
                UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
                TaskInfo taskInfo = filterToOne(engineService.getTasks(userToken), task
                        -> taskId.equals(task.getId()))
                        .orElseThrow(() -> new ClientErrorException("Task with id " + taskId + " doesn't exists anymore", Status.GONE));
                return engineService.getTaskResponse(userToken, taskInfo);
            } catch (UserNotFoundException | TaskNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            } catch (TaskOutOfDateException ex) {
                throw new ClientErrorException(ex.getMessage(), Status.GONE, ex);
            }
        }

        @Override
        public Boolean submit(Long taskId, TaskRequest taskRequest) {
            try {
                UserToken userToken = retrieveToken(SecurityContextHolder.getContext().getAuthentication());
                if (!taskId.equals(taskRequest.getId())) {
                    throw new ClientErrorException("Task id " + taskId + " doesnt match TaskRequest " + taskRequest.getId(), Status.BAD_REQUEST);
                }
                return engineService.executeTask(userToken, taskRequest);
            } catch (UserNotFoundException | TaskNotFoundException ex) {
                throw new NotFoundException(ex.getMessage(), ex);
            } catch (TaskOutOfDateException ex) {
                throw new ClientErrorException(ex.getMessage(), Status.GONE, ex);
            }
        }

    }

}