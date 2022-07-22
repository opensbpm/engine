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

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.builder.ProcessBuilder;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.engine.UserService;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isProcessInstanceChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserProcessInstanceChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserProcessModelChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserTaskChangedEvent;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

public class EngineServiceBoundaryIT extends ServiceITCase {

    @Autowired
    private UserTokenServiceBoundary userTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private EngineServiceBoundary engineService;

    @Autowired
    private ModelServiceBoundary modelService;

    private UserToken user1;
    private UserToken user2;
    private UserToken user3;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        doInTransaction(() -> {
            user1 = userTokenService.registerUser(createTokenRequest("user1", "starter-role"));
            user2 = userTokenService.registerUser(createTokenRequest("user2", "starter-role"));
            user3 = userTokenService.registerUser(createTokenRequest("user3", "role2"));

            return null;
        });
    }

    @Test
    public void startProcess() throws Exception {
        //given
        ProcessDefinition processDefinition = new ProcessBuilder("Process")
                .addSubject(userSubject("Starter", "role1").asStarter()
                        .addState(functionState("Start").asStart()
                                .toHead(functionState("End").asEnd()))
                )
                .addSubject(userSubject("Subject 2", "role2")
                        .addState(functionState("Start").asStart()
                                .toHead(functionState("End").asEnd()))
                ).build();
        ModelRequest modelRequest = ModelRequest.of(modelService.save(processDefinition));

        engineEventsCollector.clear();

        //when
        TaskInfo result = engineService.startProcess(user1, modelRequest);

        //then
        assertThat(result.getProcessId(), is(notNullValue()));

        assertThat(engineEventsCollector, hasItem(isProcessInstanceChangedEvent(Type.CREATE)));
        assertThat(engineEventsCollector, hasItem(isUserTaskChangedEvent(getId(user1), Type.CREATE)));
        assertThat(engineEventsCollector, not(hasItem(isUserProcessModelChangedEvent(getId(user2)))));
        assertThat(engineEventsCollector, not(hasItem(isUserProcessModelChangedEvent(getId(user3)))));

    }

    @Test
    public void stopProcess() throws Exception {
        //given
        ProcessDefinition processDefinition = new ProcessBuilder("Process")
                .addSubject(userSubject("Starter", "role1").asStarter()
                        .addState(functionState("Start").asStart()
                                .toHead(functionState("End").asEnd()))
                )
                .addSubject(userSubject("Subject 2", "role2")
                        .addState(functionState("Start").asStart()
                                .toHead(functionState("End").asEnd()))
                ).build();
        ModelRequest modelRequest = ModelRequest.of(modelService.save(processDefinition));
        TaskInfo taskInfo = engineService.startProcess(user1, modelRequest);

        TaskResponse taskResponse = engineService.getTaskResponse(new UserToken(), taskInfo);

        NextState next = taskResponse.getNextStates().iterator().next();
        TaskRequest taskRequest = new TaskRequest(taskInfo.getId(), next, LocalDateTime.now());

        engineEventsCollector.clear();

        //when
        doInTransaction(() -> {
            engineService.executeTask(user1, taskRequest);
            return null;
        });

        //then
        assertThat(engineEventsCollector, hasItem(isProcessInstanceChangedEvent(Type.UPDATE)));
        assertThat(engineEventsCollector, hasItem(isUserProcessInstanceChangedEvent(getId(user1), Type.DELETE)));
        assertThat(engineEventsCollector, hasItem(isUserTaskChangedEvent(getId(user1), Type.DELETE)));
        assertThat(engineEventsCollector, not(hasItem(isUserProcessModelChangedEvent(getId(user2)))));
    }

    private Long getId(UserToken userToken) {
        return userToken.getId();
    }
}
