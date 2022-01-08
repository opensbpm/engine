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

import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import javax.persistence.PersistenceException;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasEntry;

import org.hibernate.exception.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FunctionStateIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        FunctionState functionState = new FunctionState();

        //when
        FunctionState result = entityManager.persistFlushFind(functionState);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel();

        FunctionState functionState = subjectModel.addFunctionState("function");
        functionState.putParameter("parameter1", "1");
        functionState.putParameter("parameter2", "2");

        //when
        FunctionState result = entityManager.persistFlushFind(functionState);

        //then
        assertThat(result.getParameters(), allOf(
                hasEntry("parameter1", "1"),
                hasEntry("parameter2", "2")
        ));

    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel();

        FunctionState functionState = entityManager.persist(subjectModel.addFunctionState("Function"));
        assertThat(functionState.getId(), is(notNullValue()));

        //when
        FunctionState result = entityManager.persistFlushFind(subjectModel.addFunctionState("Function"));

        //then
        fail("persist of the same FunctionState twice must throw exception, but was " + result);
    }
}
