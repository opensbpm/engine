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

import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ReceiveStateIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ReceiveState receiveState = new ReceiveState();

        //when
        ReceiveState result = entityManager.persistFlushFind(receiveState);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel();

        ReceiveState receiveState = subjectModel.addReceiveState("Receive");

        //when
        ReceiveState result = entityManager.persistFlushFind(receiveState);

        //then
        assertThat(result.getId(), is(notNullValue()));

    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel();

        ReceiveState receiveState = entityManager.persist(subjectModel.addReceiveState("Receive"));
        assertThat(receiveState.getId(), is(notNullValue()));

        //when
        ReceiveState result = entityManager.persistFlushFind(subjectModel.addReceiveState("Receive"));

        //then
        fail("persist of the same ReceiveState twice must throw exception, but was " + result);
    }
}
