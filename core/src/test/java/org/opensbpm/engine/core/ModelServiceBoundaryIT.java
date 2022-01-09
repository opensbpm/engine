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
package org.opensbpm.engine.core;

import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.RbacBuilder;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isProcessModelChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isRoleChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isRoleUserChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserProcessModelChangedEvent;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

public class ModelServiceBoundaryIT extends ServiceITCase {

    @Autowired
    private ModelServiceBoundary modelService;

    private User starterUser;
    private User subjectUser;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        doInTransaction(() -> {
            Role starterRole = new Role("starter-role");
            entityManager.persist(starterRole);

            starterUser = RbacBuilder.createUser("starter-user", "firstname", "lastname")
                    .withRole(starterRole)
                    .build(entityManager);
            Role subjectRole = new Role("subject-role");
            entityManager.persist(subjectRole);

            subjectUser = RbacBuilder.createUser("subject-user", "firstname", "lastname")
                    .withRole(subjectRole)
                    .build(entityManager);
            return null;
        });
    }

    @Test
    public void saveAutoincrement() {
        //given
        ProcessDefinition processDefinition = process("Process")
                .addSubject(userSubject("starter", "starter-role").asStarter())
                .build();
        ProcessModelInfo modelV00 = modelService.save(processDefinition);
        assertThat(modelV00.getName(), is("Process"));
        assertThat(modelV00.getVersion(), is("0.0"));

        //when
        ProcessModelInfo modelV01 = modelService.save(processDefinition);

        //then
        assertThat(modelV01.getName(), is("Process"));
        assertThat(modelV01.getVersion(), is("0.1"));
    }

    @Test
    public void saveInsertAssertEvents() {
        //given
        ProcessDefinition processDefinition = process("process")
                .addSubject(userSubject("starter", "starter-role").asStarter())
                .addSubject(userSubject("subject", "subject-role"))
                .addSubject(userSubject("newsubject", "newsubject-role"))
                .build();

        engineEventsCollector.clear();

        //when
        ProcessModelInfo result = modelService.save(processDefinition);

        //then
        assertThat(result.getId(), is(notNullValue()));

        assertThat(engineEventsCollector, containsInAnyOrder(
                isProcessModelChangedEvent("process", Type.CREATE),
                isUserProcessModelChangedEvent(starterUser.getId(), Type.CREATE),
                isRoleChangedEvent("starter-role", Type.UPDATE),
                isRoleChangedEvent("subject-role", Type.UPDATE),
                isRoleChangedEvent("newsubject-role", Type.CREATE),
                isRoleUserChangedEvent(starterUser.getId(), Type.CREATE),
                isRoleUserChangedEvent(subjectUser.getId(), Type.CREATE)
        ));
        assertThat(engineEventsCollector, not(hasItem(isUserProcessModelChangedEvent(subjectUser.getId()))));
    }

    @Test
    public void updateStateAssertEvents() throws Exception {
        //given
        ProcessDefinition processDefinition = process("process")
                .addSubject(userSubject("starter", "starter-role").asStarter())
                .addSubject(userSubject("subject", "subject-role"))
                .build();
        ProcessModelInfo modelInfo = modelService.save(processDefinition);

        engineEventsCollector.clear();

        //when
        modelService.updateState(modelInfo, ProcessModelState.INACTIVE);

        //then
        assertThat(engineEventsCollector, containsInAnyOrder(
                isProcessModelChangedEvent("process", Type.UPDATE),
                isUserProcessModelChangedEvent(starterUser.getId(), Type.DELETE)
        ));
        assertThat(engineEventsCollector, not(hasItem(isUserProcessModelChangedEvent(subjectUser.getId()))));
    }

    @Test
    public void deleteAssertEvents() throws Exception {
        //given
        ProcessDefinition processDefinition = process("process")
                .addSubject(userSubject("starter", "starter-role").asStarter())
                .addSubject(userSubject("subject", "subject-role"))
                .build();
        ProcessModelInfo modelInfo = modelService.save(processDefinition);
        engineEventsCollector.clear();

        //when
        modelService.delete(modelInfo);

        //then
        assertThat(engineEventsCollector, containsInAnyOrder(
                isProcessModelChangedEvent("process", Type.DELETE),
                isUserProcessModelChangedEvent(starterUser.getId(), Type.DELETE)
        ));
        assertThat(engineEventsCollector, not(hasItem(isUserProcessModelChangedEvent(subjectUser.getId()))));
    }

}
