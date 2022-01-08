/*******************************************************************************
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
 ******************************************************************************/
package org.opensbpm.engine.api;

import java.util.Collection;
import org.opensbpm.engine.api.ModelService.ModelRequest;
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

/**
 * Service interface to query and execute user specific SBPM processes
 *
 */
public interface EngineService {

    /**
     * find all startable Processmodels for the given {@link UserToken}.A ProcessModel is startable when the
     * processmodel is in active state and the user is in the role of start subject
     *
     * @param userToken
     * @return
     * @throws UserNotFoundException
     */
    Collection<ProcessModelInfo> findStartableProcessModels(UserToken userToken) throws UserNotFoundException;

    /**
     * start the given processmodel
     *
     * @param userToken
     * @param modelRequest
     * @return
     * @throws UserNotFoundException
     * @throws ModelNotFoundException
     */
    TaskInfo startProcess(UserToken userToken, ModelRequest modelRequest)
            throws UserNotFoundException, ModelNotFoundException;

    /**
     * find all processes with the given state from given user.
     *
     * @param userToken
     * @param state
     * @return
     * @throws UserNotFoundException
     */
    Collection<ProcessInfo> findAllByUserAndState(UserToken userToken, ProcessInstanceState state)
            throws UserNotFoundException;
    //TODO add find-method with SearchFilter

    /**
     * retrieve a overview of all executable tasks
     *
     * @param userToken
     * @return
     * @throws UserNotFoundException
     */
    Collection<TaskInfo> getTasks(UserToken userToken) throws UserNotFoundException;

    /**
     * retrieve all necersary data to execute the task
     *
     * @param userToken
     * @param taskInfo
     * @return
     * @throws TaskNotFoundException
     * @throws TaskOutOfDateException
     */
    TaskResponse getTaskResponse(UserToken userToken, TaskInfo taskInfo)
            throws TaskNotFoundException, TaskOutOfDateException;

    /**
     * retrieve autocomplete data
     *
     * @param userToken
     * @param taskInfo
     * @return
     * @throws TaskNotFoundException
     * @throws TaskOutOfDateException
     */
    AutocompleteResponse getAutocompleteResponse(UserToken userToken, TaskInfo taskInfo, ObjectRequest objectRequest, String queryString)
            throws TaskNotFoundException, TaskOutOfDateException;

    /**
     * executes the task and change the state of the process to the next state
     *
     * @param userToken
     * @param taskRequest
     * @return
     * @throws UserNotFoundException
     * @throws TaskNotFoundException
     * @throws TaskOutOfDateException
     */
    Boolean executeTask(UserToken userToken, TaskRequest taskRequest)
            throws UserNotFoundException, TaskNotFoundException, TaskOutOfDateException;

    public static interface ObjectRequest {

        public static ObjectRequest of(Long id) {
            return () -> id;
        }

        Long getId();
    }
}
