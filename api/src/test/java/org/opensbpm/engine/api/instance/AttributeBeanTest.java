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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.opensbpm.engine.api.instance.Task.AttributeBean;
import org.opensbpm.engine.api.model.Binary;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Theories.class)
public class AttributeBeanTest {

    private static final String FIELDVALUES = "fieldValues";

    @DataPoints(FIELDVALUES)
    public static FieldValue[] FIELDVALUES_DATA = new FieldValue[]{
        new FieldValue(FieldType.STRING, "1"),
        new FieldValue(FieldType.NUMBER, 1),
        new FieldValue(FieldType.DECIMAL, BigDecimal.TEN),
        new FieldValue(FieldType.DATE, LocalDate.now()),
        new FieldValue(FieldType.TIME, LocalTime.now()),
        new FieldValue(FieldType.BOOLEAN, true),
        new FieldValue(FieldType.BINARY, new Binary("application/pdf", new byte[]{}))
    };

    @Theory
    @Test
    public void checkAndSetValue(FieldValue fieldValue) {
        //given
        AttributeSchema attributeSchema = new AttributeSchema(Long.MIN_VALUE, "name", fieldValue.fieldType);

        ObjectSchema objectSchema = ObjectSchema.of(1l, "Object", singletonList(attributeSchema));
        TaskResponse taskResponse = TaskResponse.of(Long.MIN_VALUE, 
                Collections.emptyList(), 
                LocalDateTime.MIN, 
                asList(objectSchema), 
                emptyList()
        );

        Task task = new Task(new TaskInfo(), taskResponse);

        AttributeBean attributeBean = task.getTaskDocument().getAttribute(objectSchema, attributeSchema);

        //when
        attributeBean.setValue(fieldValue.value);

        //then
        assertThat(attributeBean.getValue(), is(fieldValue.value));
    }

    public static class FieldValue {

        private final FieldType fieldType;
        private final Serializable value;

        public FieldValue(FieldType fieldType, Serializable value) {
            this.fieldType = fieldType;
            this.value = value;
        }

    }

}
