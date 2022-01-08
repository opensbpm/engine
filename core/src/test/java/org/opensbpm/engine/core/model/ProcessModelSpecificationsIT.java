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
package org.opensbpm.engine.core.model;

import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.junit.DataJpaTestCase;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.model.ProcessModelService.ProcessModelRepository;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.opensbpm.engine.core.model.ProcessModelService.ProcessModelSpecifications.withStartableFor;

public class ProcessModelSpecificationsIT extends DataJpaTestCase {

    @Autowired
    private ProcessModelRepository repository;

    @Test
    public void withStartableForRole() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));

        Role role1 = new Role("role-1");
        UserSubjectModel subject1 = processModel.addUserSubjectModel("subject-1", Arrays.asList(role1));
        processModel.setStarterSubject(subject1);
        subject1.addFunctionState("state-1").setEventType(StateEventType.START);

        Role role2 = new Role("role-2");
        UserSubjectModel subject2 = processModel.addUserSubjectModel("subject-2", Arrays.asList(role2));
        subject2.addFunctionState("state-2").setEventType(StateEventType.START);

        processModel = entityManager.persistFlushFind(processModel);
        role1 = entityManager.find(Role.class, role1.getId());
        role2 = entityManager.find(Role.class, role2.getId());

        //when        
        List<ProcessModel> result1 = repository.findAll(withStartableFor(role1));
        List<ProcessModel> result2 = repository.findAll(withStartableFor(role2));

        //then
        assertThat(result1, hasSize(1));
        assertThat(result1, hasItem(hasProperty("name", is(processModel.getName()))));

        assertThat(result2, hasSize(0));
    }

    @Test
    public void withStartableForUser() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));

        Role role1 = new Role("role-1");
        UserSubjectModel subject1 = processModel.addUserSubjectModel("subject-1", Arrays.asList(role1));
        processModel.setStarterSubject(subject1);
        subject1.addFunctionState("state-1").setEventType(StateEventType.START);

        Role role2 = new Role("role-2");
        UserSubjectModel subject2 = processModel.addUserSubjectModel("subject-2", Arrays.asList(role2));
        subject2.addFunctionState("state-2").setEventType(StateEventType.START);

        User user1 = entityManager.persist(new User("user1"));
        role1.addUser(user1);

        User user2 = entityManager.persist(new User("user2"));
        role2.addUser(user2);

        processModel = entityManager.persistFlushFind(processModel);
        user1 = entityManager.find(User.class, user1.getId());
        user2 = entityManager.find(User.class, user2.getId());

        //when        
        List<ProcessModel> result1 = repository.findAll(withStartableFor(user1));
        List<ProcessModel> result2 = repository.findAll(withStartableFor(user2));

        //then
        assertThat(result1, hasSize(1));
        assertThat(result1, hasItem(hasProperty("name", is(processModel.getName()))));

        assertThat(result2, hasSize(0));
    }

}
