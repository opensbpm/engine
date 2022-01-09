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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class UserSubjectIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        UserSubject userSubject = new UserSubject();

        //when
        UserSubject result = entityManager.persistFlushFind(userSubject);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        UserSubjectModel userSubjectModel = persistedUserSubjectModel(processModel, "Subject", Arrays.asList(new Role("Role")));
        ProcessInstance processInstance = persistedProcessInstance(processModel);

        UserSubject userSubject = new UserSubject(processInstance, userSubjectModel, null);

        //when
        userSubject = entityManager.persistFlushFind(userSubject);

        //then
        assertThat(userSubject.getId(), is(notNullValue()));
        assertThat(userSubject.getProcessInstance().getId(), is(processInstance.getId()));
    }

}
