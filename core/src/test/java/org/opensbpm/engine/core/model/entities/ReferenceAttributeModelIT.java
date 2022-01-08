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
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import javax.persistence.PersistenceException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.hibernate.exception.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReferenceAttributeModelIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ReferenceAttributeModel referenceAttributeModel = new ReferenceAttributeModel();

        //when
        ReferenceAttributeModel result = entityManager.persistFlushFind(referenceAttributeModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();
        ObjectModel objectModel = persistedObjectModel(processModel,"object");
        ObjectModel referenceObjectModel = persistedObjectModel(processModel,"refobject");
        
        ReferenceAttributeModel referenceAttributeModel = objectModel.addAttributeModel(new ReferenceAttributeModel(objectModel, "attributeName", referenceObjectModel));

        //when
        ReferenceAttributeModel result = entityManager.persistFlushFind(referenceAttributeModel);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("attributeName"));
        assertThat(result.getPosition(), is(1));
    }

    @Test
    public void testUniqueConstraintObjectModelAndName() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ProcessModel processModel = persistedProcessModel();
        ObjectModel objectModel = persistedObjectModel(processModel,"object");
        ObjectModel referenceObjectModel = persistedObjectModel(processModel,"refobject");

        ReferenceAttributeModel referenceAttributeModel = entityManager.persistFlushFind(objectModel.addAttributeModel(new ReferenceAttributeModel(objectModel, "attributeName", referenceObjectModel)));
        assertThat(referenceAttributeModel.getId(), is(notNullValue()));

        objectModel = entityManager.merge(objectModel);

        //when
        ReferenceAttributeModel result = entityManager.persistFlushFind(objectModel.addAttributeModel(new ReferenceAttributeModel(objectModel, "attributeName", referenceObjectModel)));

        //then
        fail("insert with existing ObjectModel and Name must throw Exception, but was " + result);
    }

}
