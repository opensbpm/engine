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
import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ServiceSubjectIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ServiceSubject serviceSubject = new ServiceSubject();

        //when
        ServiceSubject result = entityManager.persistFlushFind(serviceSubject);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ServiceSubjectModel serviceSubjectModel = persistedServiceSubjectModel(processModel, "Subject");
        ProcessInstance processInstance = persistedProcessInstance(processModel);

        ServiceSubject serviceSubject = new ServiceSubject(processInstance, serviceSubjectModel);

        //when
        serviceSubject = entityManager.persistFlushFind(serviceSubject);

        //then
        assertThat(serviceSubject.getId(), is(notNullValue()));
        assertThat(serviceSubject.getProcessInstance().getId(), is(processInstance.getId()));
    }

}
