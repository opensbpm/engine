/** *****************************************************************************
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
 *****************************************************************************
 */
package org.opensbpm.engine.core.engine.entities;

import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class ObjectInstanceIT extends EntityDataTestCase {

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
        //given
        ProcessModel processModel = persistedProcessModel();
        ObjectModel objectModel = persistedObjectModel(processModel);

        ProcessInstance processInstance = persistedProcessInstance(processModel);
        ObjectInstance objectInstance = processInstance.addObjectInstance(objectModel);

        objectInstance = entityManager.persist(objectInstance);
        assertThat(objectInstance.getId(), is(notNullValue()));

        ObjectInstance newObjectInstance = processInstance.addObjectInstance(objectModel);

        //when        
        PersistenceException persistenceException = assertThrows("UniqueKey doesn't work", PersistenceException.class, () -> {
            ObjectInstance result = entityManager.persistFlushFind(newObjectInstance);
            fail("persist of the same ObjectInstance twice must throw exception, but was " + result);
        });

        //then
        assertThat("UniqueKey doesn't work", persistenceException,
                is(instanceOf(ConstraintViolationException.class)));
    }

}
