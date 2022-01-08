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

import org.opensbpm.engine.core.engine.UserSubjectService;
import org.opensbpm.engine.core.engine.ProcessInstanceService;
import org.opensbpm.engine.core.engine.SubjectService;

import static org.opensbpm.engine.utils.StreamUtils.filterToOne;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.junit.RbacBuilder;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserSubjectServiceIT extends ServiceITCase {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserSubjectService userSubjectService;

    private Role starterRole;
    private Role subject2Role;
    private Role otherRole;

    private User starterUser1;
    private User starterUser2;
    private User subject2User1;
    private User subject2User2;
    private User otherRoleUser;

    private ProcessModel processModel;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        processModel = doInTransaction(() -> {
            starterRole = new Role("Starter - Role");
            entityManager.persist(starterRole);

            subject2Role = new Role("Subject 2 - Role");
            entityManager.persist(subject2Role);

            otherRole = new Role("Other Role");
            entityManager.persist(otherRole);

            starterUser1 = RbacBuilder.createUser("Starter - User 1", "firstname", "lastname")
                    .withRole(starterRole)
                    .build(entityManager);
            starterUser2 = RbacBuilder.createUser("Starter - User 2", "firstname", "lastname")
                    .withRole(starterRole)
                    .build(entityManager);

            subject2User1 = RbacBuilder.createUser("Subject 2 - User 1", "firstname", "lastname")
                    .withRole(subject2Role)
                    .build(entityManager);

            subject2User2 = RbacBuilder.createUser("Subject 2 - User 2", "firstname", "lastname")
                    .withRole(subject2Role)
                    .build(entityManager);

            otherRoleUser = RbacBuilder.createUser("Other Role - User", "firstname", "lastname")
                    .withRole(otherRole)
                    .build(entityManager);

            ProcessDefinition processDefinition = process("Process")
                    .addSubject(userSubject("Starter", starterRole.getName())
                            .asStarter()
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    )
                    .addSubject(userSubject("Subject 2", subject2Role.getName())
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("End").asEnd()))
                    ).build();

            return saveProcessDefinition(processDefinition);
        });
    }

    @Test
    public void querySubjectsOfUsers() {
        //given

        //when 
        UserSubject unassignedSubject = doInTransaction(() -> {
            ProcessInstance processInstance = processInstanceService.start(processModel, starterUser1);
            subjectService.createSubject(processInstance, processModel.getStarterSubjectModel(), starterUser1);

            //initialize unassigned UserSubject
            UserSubjectModel subject2Model = filterToOne(processModel.getUserSubjectModels(),
                    subjectModel -> subjectModel.getName().equals("Subject 2"))
                    .orElseThrow(() -> new IllegalStateException("Subject 2 not found"));
            return (UserSubject) subjectService.createSubject(processInstance, subject2Model, null);
        });
        assertThat(unassignedSubject.getId(), is(notNullValue()));
        assertThat(unassignedSubject.getUser(), is(nullValue()));

        List<UserSubject> subjects;

        subjects = userSubjectService.findAllByUser(starterUser1);
        assertThat("starterSubject found", subjects, hasSize(1));

        subjects = userSubjectService.findAllByUser(starterUser2);
        assertThat("subject for non-starter found", subjects, hasSize(0));

        subjects = userSubjectService.findAllByUser(subject2User1);
        assertThat("unassigned subject with 'Subject 2- Role' not found", subjects, hasSize(1));

        subjects = userSubjectService.findAllByUser(subject2User2);
        assertThat("unassigned subject with 'Subject 2- Role' not found", subjects, hasSize(1));

        subjects = userSubjectService.findAllByUser(otherRoleUser);
        assertThat("unassigned subject with 'Other Role' not found", subjects, hasSize(0));
    }

}
