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

import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import javax.persistence.PersistenceException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.hibernate.exception.ConstraintViolationException;

import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ObjectModelIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ObjectModel objectModel = new ObjectModel();

        //when
        ObjectModel result = entityManager.persistFlushFind(objectModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ObjectModel objectModel = processModel.addObjectModel("name");

        //when
        ObjectModel result = entityManager.persistFlushFind(objectModel);

        //then
        assertNotNull(result.getId());
        assertThat(result.getName(), is("name"));
    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ProcessModel processModel = persistedProcessModel();

        ObjectModel objectModel = entityManager.persistFlushFind(processModel.addObjectModel("object"));
        assertThat(objectModel.getId(), is(notNullValue()));

        processModel = entityManager.merge(processModel);

        //when
        ObjectModel result = entityManager.persistFlushFind(processModel.addObjectModel("object"));

        //then
        fail("persist of the same ObjectModel twice must throw exception, but was " + result);
    }

}
