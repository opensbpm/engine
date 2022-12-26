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
package org.opensbpm.engine.core.model.entities;

import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class MessageModelIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        MessageModel messageModel = new MessageModel();

        //when
        MessageModel result = entityManager.persistFlushFind(messageModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel(processModel);
        ReceiveState receiveState = persistedReceiveState(subjectModel);
        FunctionState functionState = persistedFunctionState(subjectModel);
        ObjectModel objectModel = persistedObjectModel(processModel);

        MessageModel messageModel = receiveState.addMessageModel(objectModel, functionState);

        //when
        MessageModel result = entityManager.persistFlushFind(messageModel);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getObjectModel(), is(notNullValue()));
        assertThat(result.getHead(), is(notNullValue()));
    }

    @Test
    public void testUniqueConstraint() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel(processModel);
        ReceiveState receiveState = persistedReceiveState(subjectModel);
        FunctionState functionState = persistedFunctionState(subjectModel);
        ObjectModel objectModel = persistedObjectModel(processModel);

        MessageModel messageModel = entityManager.persist(receiveState.addMessageModel(objectModel, functionState));
        assertThat(messageModel.getId(), is(notNullValue()));

        MessageModel newMessageModel = receiveState.addMessageModel(objectModel, functionState);

        //when
        PersistenceException persistenceException = assertThrows("UniqueKey doesn't work", PersistenceException.class, () -> {
            MessageModel result = entityManager.persistFlushFind(newMessageModel);
            fail("persist of the same MessageModel twice must throw exception, but was " + result);
        });

        //then
        assertThat("UniqueKey doesn't work", persistenceException.getCause(),
                is(instanceOf(ConstraintViolationException.class)));
    }

}
