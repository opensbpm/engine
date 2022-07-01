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

public class ServiceSubjectModelIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ServiceSubjectModel subjectModel = new ServiceSubjectModel();

        //when
        ServiceSubjectModel result = entityManager.persistFlushFind(subjectModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test(expected = PersistenceException.class)
    public void testInsertWithNullName() {
        //given
        ServiceSubjectModel subjectModel = persistedProcessModel().addServiceSubjectModel(null);

        //when
        ServiceSubjectModel result = entityManager.persistFlushFind(subjectModel);

        //then
        fail("persist with null-name must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ServiceSubjectModel subjectModel = persistedProcessModel().addServiceSubjectModel("Subject");

        //when
        ServiceSubjectModel result = entityManager.persistFlushFind(subjectModel);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("Subject"));
    }

    @Test
    public void testUniqueConstraint() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ServiceSubjectModel subjectModel = entityManager.persist(processModel.addServiceSubjectModel("Subject"));
        assertThat(subjectModel.getId(), is(notNullValue()));

        ServiceSubjectModel newSubjectModel = processModel.addServiceSubjectModel("Subject");

        //when
        PersistenceException persistenceException = assertThrows("UniqueKey doesn't work", PersistenceException.class, () -> {
            ServiceSubjectModel result = entityManager.persistFlushFind(newSubjectModel);
            fail("persist of the same ServiceSubjectModel twice must throw exception, but was " + result);
        });

        //then
        assertThat("UniqueKey doesn't work", persistenceException.getCause(),
                is(instanceOf(ConstraintViolationException.class)));
    }

}
