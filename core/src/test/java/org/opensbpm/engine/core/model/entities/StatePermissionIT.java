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
import org.opensbpm.engine.core.model.entities.StatePermission;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
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

public class StatePermissionIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        StatePermission statePermission = new StatePermission();

        //when
        StatePermission result = entityManager.persistFlushFind(statePermission);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = persistedProcessModel();

        SubjectModel subjectModel = persistedServiceSubjectModel(processModel);
        FunctionState state = persistedFunctionState(subjectModel);
        ObjectModel objectModel = persistedObjectModel(processModel);
        SimpleAttributeModel simpleAttributeModel = entityManager.persist(objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "fieldName", FieldType.STRING)));

        StatePermission statePermission = state.addStatePermission(simpleAttributeModel, Permission.READ);
        statePermission.setMandatory(true);

        //when
        StatePermission result = entityManager.persistFlushFind(statePermission);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getAttributeModel().getId(), is(notNullValue()));
        assertThat(result.getPermission(), is(Permission.READ));
        assertThat(result.isMandatory(), is(true));
    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ProcessModel processModel = persistedProcessModel();

        SubjectModel subjectModel = persistedServiceSubjectModel();
        FunctionState state = persistedFunctionState(subjectModel);
        ObjectModel objectModel = persistedObjectModel(processModel);
        SimpleAttributeModel simpleAttributeModel = entityManager.persist(objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "fieldName", FieldType.STRING)));

        StatePermission statePermission = entityManager.persist(state.addStatePermission(simpleAttributeModel, Permission.READ));
        assertThat(statePermission.getId(), is(notNullValue()));

        //when
        StatePermission result = entityManager.persistFlushFind(state.addStatePermission(simpleAttributeModel, Permission.READ));

        //then
        fail("persist of the same ReceiveState twice must throw exception, but was " + result);
    }

}
