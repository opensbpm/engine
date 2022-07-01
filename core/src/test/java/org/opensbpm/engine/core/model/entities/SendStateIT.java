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

public class SendStateIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        SendState sendState = new SendState();

        //when
        SendState result = entityManager.persistFlushFind(sendState);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel(processModel, "Subject");
        ServiceSubjectModel receiver = persistedServiceSubjectModel(processModel, "Receiver");
        ObjectModel objectModel = persistedObjectModel(processModel);

        SendState sendState = subjectModel.addSendState("Send", receiver, objectModel);

        //when
        SendState result = entityManager.persistFlushFind(sendState);

        //then
        assertThat(result.getId(), is(notNullValue()));

    }

    @Test
    public void testUniqueConstraint() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ServiceSubjectModel subjectModel = persistedServiceSubjectModel(processModel, "Subject");
        ServiceSubjectModel receiver = persistedServiceSubjectModel(processModel, "Receiver");
        ObjectModel objectModel = persistedObjectModel(processModel);

        SendState sendState = entityManager.persist(subjectModel.addSendState("Send", receiver, objectModel));
        assertThat(sendState.getId(), is(notNullValue()));

        SendState newSendState = subjectModel.addSendState("Send", receiver, objectModel);

        //when
        PersistenceException persistenceException = assertThrows("UniqueKey doesn't work", PersistenceException.class, () -> {
            SendState result = entityManager.persistFlushFind(newSendState);
            fail("persist of the same SendState twice must throw exception, but was " + result);
        });

        //then
        assertThat("UniqueKey doesn't work", persistenceException.getCause(),
                is(instanceOf(ConstraintViolationException.class)));
    }

}
