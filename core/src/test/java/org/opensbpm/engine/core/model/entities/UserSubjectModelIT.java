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
package org.opensbpm.engine.core.model.entities;

import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import java.util.Arrays;
import javax.persistence.PersistenceException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

import org.hibernate.exception.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UserSubjectModelIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        UserSubjectModel subjectModel = new UserSubjectModel();

        //when
        UserSubjectModel result = entityManager.persistFlushFind(subjectModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test(expected = PersistenceException.class)
    public void testInsertWithNullName() {
        //given
        UserSubjectModel subjectModel = persistedProcessModel().addUserSubjectModel(null, Arrays.asList(new Role("name")));

        //when
        UserSubjectModel result = entityManager.persistFlushFind(subjectModel);

        //then
        fail("persist with null-name must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        UserSubjectModel subjectModel = persistedProcessModel().addUserSubjectModel("Subject", Arrays.asList(new Role("name")));

        //when
        UserSubjectModel result = entityManager.persistFlushFind(subjectModel);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("Subject"));
        assertThat(result.getRoles(), hasSize(1));
    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ProcessModel processModel = persistedProcessModel();
        UserSubjectModel subjectModel = entityManager.persist(processModel.addUserSubjectModel("Subject", Arrays.asList(new Role("name"))));
        assertThat(subjectModel.getId(), is(notNullValue()));

        //when
        UserSubjectModel result = entityManager.persistFlushFind(processModel.addUserSubjectModel("Subject", Arrays.asList(new Role("name"))));

        //then
        fail("persist of the same UserSubjectModel twice must throw exception, but was " + result);
    }

}
