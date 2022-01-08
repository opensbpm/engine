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

import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
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

public class ObjectInstanceIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ObjectInstance objectInstance = new ObjectInstance();

        //when
        ObjectInstance result = entityManager.persistFlushFind(objectInstance);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ObjectModel objectModel = persistedObjectModel(processModel);

        ProcessInstance processInstance = persistedProcessInstance(processModel);

        ObjectInstance objectInstance = new ObjectInstance(objectModel, processInstance);

        //when
        ObjectInstance result = entityManager.persistFlushFind(objectInstance);

        //then
        assertNotNull(result.getId());
        assertThat(result.getObjectModel(), is(notNullValue()));
        assertThat(result.getProcessInstance(), is(notNullValue()));
    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ProcessModel processModel = persistedProcessModel();
        ObjectModel objectModel = persistedObjectModel(processModel);

        ProcessInstance processInstance = persistedProcessInstance(processModel);
        ObjectInstance objectInstance = processInstance.addObjectInstance(objectModel);

        objectInstance = entityManager.persist(objectInstance);
        assertThat(objectInstance.getId(), is(notNullValue()));

        //when
        objectInstance = processInstance.addObjectInstance(objectModel);
        ObjectInstance result = entityManager.persistFlushFind(objectInstance);

        //then
        fail("persist of the same ObjectInstance twice must throw exception, but was " + result);
    }

}
