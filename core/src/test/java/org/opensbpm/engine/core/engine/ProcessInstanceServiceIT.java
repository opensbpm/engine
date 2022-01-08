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
package org.opensbpm.engine.core.engine;

import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.junit.RbacBuilder;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import org.junit.After;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.serviceSubject;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

import org.opensbpm.engine.api.model.definition.ProcessDefinition;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;

public class ProcessInstanceServiceIT extends ServiceITCase {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ProcessModelService processModelService;

    @Autowired
    private SubjectService subjectService;

    private Role starterRole;
    private Role role2;
    private User user1;
    private User user2;
    private User user3;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        doInTransaction(() -> {
            starterRole = new Role("starter-role");
            role2 = new Role("role2");

            user1 = RbacBuilder.createUser("user1", "firstname", "lastname").withRole(starterRole).build(entityManager);
            user2 = RbacBuilder.createUser("user2", "firstname", "lastname").withRole(starterRole).build(entityManager);
            user3 = RbacBuilder.createUser("user3", "firstname", "lastname").withRole(role2).build(entityManager);
            return null;
        });
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    private ProcessModel createProcessModel() {
        return doInTransaction(() -> {
            ProcessDefinition processDefinition = process("name")
                    .addSubject(userSubject("Starter", starterRole.getName())
                            .asStarter()
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    )
                    .addSubject(userSubject("Subject 1", role2.getName())
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    )
                    .addSubject(serviceSubject("Service 1")
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    ).build();
            return saveProcessDefinition(processDefinition);
        });
    }

    @Test
    public void testGetProcessesInfoOfUserAndState() {
        //given
        ProcessInstance processInstance = doInTransaction(() -> {
            ProcessModel processModel = createProcessModel();
            ProcessInstance instance = processInstanceService.start(processModel, user1);
            subjectService.createSubject(instance, processModel.getStarterSubjectModel(), user1);
            return instance;
        });

        //when
        List<ProcessInstance> result = processInstanceService.findAllByUserAndState(user1, ProcessInstanceState.ACTIVE);

        //then
        assertNotNull(result);
        assertThat(result.get(0).getId(), is(processInstance.getId()));
        assertThat(result.get(0).getStartUser().getId(), is(user1.getId()));
    }

    @Test
    public void startProcess() {
        //given
        ProcessModel processModel = doInTransaction(() -> {
            ProcessDefinition processDefinition = process("Process")
                    .addSubject(userSubject("Starter", "role1")
                            .asStarter()
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    )
                    .addSubject(userSubject("Subject 2", "role2")
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    ).build();
            return saveProcessDefinition(processDefinition);
        });

        engineEventsCollector.clear();

        //when
        ProcessInstance result = doInTransaction(() -> {
            return processInstanceService.start(entityManager.merge(processModel), user1);
        });

        //then
        assertThat(result.getId(), is(notNullValue()));
    }

    @Test
    public void stopProcess() {
        //given
        ProcessInstance processInstance = doInTransaction(() -> {
            ProcessDefinition processDefinition = process("Process")
                    .addSubject(userSubject("Starter", "role1").asStarter()
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    )
                    .addSubject(userSubject("Subject 2", "role2")
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    ).build();

            ProcessModel processModel = saveProcessDefinition(processDefinition);
            ProcessInstance instance = processInstanceService.start(processModel, user1);
            subjectService.createSubject(instance, processModel.getStarterSubjectModel(), user1);
            return instance;
        });
        engineEventsCollector.clear();

        //when
        ProcessInstance result = processInstanceService.cancelByUser(processInstance);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.isActive(), is(false));

    }
}
