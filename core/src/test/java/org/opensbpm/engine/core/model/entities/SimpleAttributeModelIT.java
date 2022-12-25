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
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class SimpleAttributeModelIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        SimpleAttributeModel simpleAttributeModel = new SimpleAttributeModel();

        //when
        SimpleAttributeModel result = entityManager.persistFlushFind(simpleAttributeModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ObjectModel objectModel = persistedObjectModel();
        SimpleAttributeModel simpleAttributeModel = objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "fieldName", FieldType.STRING));

        //when
        SimpleAttributeModel result = entityManager.persistFlushFind(simpleAttributeModel);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("fieldName"));
        assertThat(result.getFieldType(), is(FieldType.STRING));
        assertThat(result.getPosition(), is(1));
        assertThat(result.getDefaultValue().orElse(null), is(nullValue()));
    }

    @Test
    public void testUniqueConstraintObjectModelAndName() {
        //given
        ObjectModel objectModel = persistedObjectModel();
        SimpleAttributeModel simpleAttributeModel = entityManager.persistFlushFind(objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "fieldName", FieldType.STRING)));
        assertThat(simpleAttributeModel.getId(), is(notNullValue()));

        objectModel = entityManager.merge(objectModel);
        SimpleAttributeModel newAttributeModel = objectModel.addAttributeModel(
                new SimpleAttributeModel(objectModel, "fieldName", FieldType.STRING));

        //when
        PersistenceException persistenceException = assertThrows("UniqueKey doesn't work", PersistenceException.class, () -> {
            SimpleAttributeModel result = entityManager.persistFlushFind(newAttributeModel);
            fail("insert with existing ObjectModel and Name must throw Exception, but was " + result);
        });

        //then
        assertThat("UniqueKey doesn't work", persistenceException.getCause(),
                is(instanceOf(ConstraintViolationException.class)));
    }

}
