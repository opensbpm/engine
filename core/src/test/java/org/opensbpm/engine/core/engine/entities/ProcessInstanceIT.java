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
package org.opensbpm.engine.core.engine.entities;

import java.util.Arrays;
import javax.persistence.PersistenceException;
import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.fail;

public class ProcessInstanceIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ProcessInstance processInstance = new ProcessInstance();

        //when
        ProcessInstance result = entityManager.persistFlushFind(processInstance);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsertCascadeSubject() {
        //given
        ProcessModel processModel = persistedProcessModel();
        UserSubjectModel userSubjectModel = persistedUserSubjectModel(processModel, "Subject", Arrays.asList(new Role("Role")));

        User user = entityManager.persist(new User("userName"));

        ProcessInstance processInstance = new ProcessInstance(processModel, user);
        UserSubject subject = new UserSubject(processInstance, userSubjectModel, null);

        //when
        processInstance = entityManager.persistFlushFind(processInstance);

        //then
        assertThat(processInstance.getId(), is(notNullValue()));
        assertThat(processInstance.getSubjects(), is(not(empty())));
        assertThat(subject.getProcessInstance().getId(), is(processInstance.getId()));
    }

}
