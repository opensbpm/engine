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
 * ****************************************************************************
 */
package org.opensbpm.engine.api.instance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import org.apache.commons.beanutils.DynaBean;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;
import org.opensbpm.engine.api.junit.ObjectSchemaBuilder;
import org.opensbpm.engine.api.model.Binary;
import org.opensbpm.engine.api.model.FieldType;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static org.apache.commons.beanutils.PropertyUtils.setProperty;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.opensbpm.engine.api.junit.ObjectSchemaBuilder.simple;

public class ObjectBeanTest {

    private static LocalDate today() {
        return LocalDate.of(2022, Month.JULY, 25);
    }

    private static LocalTime now() {
        return LocalTime.of(12, 34);
    }

    @Test
    public void testSetGetAttributes() throws Exception {
        //arrange
        ObjectSchema objectSchema = new ObjectBeanHelper().createObjetSchema();
        DynaBean dynaBean = new ObjectBean(objectSchema);

        //act + assert
        assertSetGetProperty(dynaBean, "string", "a");
        assertSetGetProperty(dynaBean, "number", 10);
        assertSetGetProperty(dynaBean, "decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "date", today());
        assertSetGetProperty(dynaBean, "time", now());
        assertSetGetProperty(dynaBean, "boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "binary", new Binary());

        assertSetGetProperty(dynaBean, "nested.string", "a");
        assertSetGetProperty(dynaBean, "nested.number", 10);
        assertSetGetProperty(dynaBean, "nested.decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "nested.date", today());
        assertSetGetProperty(dynaBean, "nested.time", now());
        assertSetGetProperty(dynaBean, "nested.boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "nested.binary", new Binary());
        assertSetGetProperty(dynaBean, "nested.nested.string", "a");
        assertSetGetProperty(dynaBean, "nested.indexed[0].string", "a");

        assertSetGetProperty(dynaBean, "indexed[0].string", "a");
        assertSetGetProperty(dynaBean, "indexed[0].number", 10);
        assertSetGetProperty(dynaBean, "indexed[0].decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "indexed[0].date", today());
        assertSetGetProperty(dynaBean, "indexed[0].time", now());
        assertSetGetProperty(dynaBean, "indexed[0].boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "indexed[0].binary", new Binary());
        assertSetGetProperty(dynaBean, "indexed[0].nested.string", "a");
        assertSetGetProperty(dynaBean, "indexed[0].indexed[0].string", "a");
    }

    @Test
    public void testSetAttributeNonExisting() throws Exception {
        //arrange
        ObjectSchema objectSchema = new ObjectBeanHelper().createObjetSchema();
        DynaBean dynaBean = new ObjectBean(objectSchema);

        //act+assert
        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "doesNotExists", null));

        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "nested.doesNotExists", null));

        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "nested.nested.doesNotExists", null));

        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "nested.indexed[0].doesNotExists", null));

        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "indexed[0].doesNotExists", null));

        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "indexed[0].nested.doesNotExists", null));

        assertThrows(NoSuchMethodException.class, ()
                -> setProperty(dynaBean, "indexed[0].indexed[0].doesNotExists", null));
    }

    @Ignore("not correct implemented yet")
    @Test
    public void testSetWrongType() throws Exception {
        //arrange
        ObjectSchema objectSchema = ObjectSchemaBuilder.schema("defaults")
                .attribute(simple("number", FieldType.NUMBER).required())
                .build();

        DynaBean dynaBean = new ObjectBean(objectSchema);

        //act + assert
        assertThrows(ClassCastException.class, ()
                -> setProperty(dynaBean, "number", LocalDate.now())
        );
    }

    @Test
    public void testSetRequiredToNull() throws Exception {
        //arrange
        ObjectSchema objectSchema = ObjectSchemaBuilder.schema("defaults")
                .attribute(simple("number", FieldType.NUMBER).required())
                .build();

        DynaBean dynaBean = new ObjectBean(objectSchema);

        //act + assert
        assertThrows(IllegalArgumentException.class, ()
                -> setProperty(dynaBean, "number", null)
        );
    }

    @Test
    public void testNestedTypes() throws Exception {
        //arrange
        ObjectSchema objectSchema = new ObjectBeanHelper().createObjetSchema();
        DynaBean dynaBean = new ObjectBean(objectSchema);

        //act + assert
        assertThat(getProperty(dynaBean, "nested"), is(instanceOf(ObjectBean.class)));
        assertThat(getProperty(dynaBean, "nested.nested"), is(instanceOf(ObjectBean.class)));
        assertThat(getProperty(dynaBean, "nested.indexed"), is(instanceOf(List.class)));

        assertThat(getProperty(dynaBean, "indexed"), is(instanceOf(List.class)));
        assertThat(getProperty(dynaBean, "indexed[0].nested"), is(instanceOf(ObjectBean.class)));
        assertThat(getProperty(dynaBean, "indexed[0].indexed"), is(instanceOf(List.class)));
    }

    @Test
    public void testGivenValues() throws Exception {
        //given

        ObjectSchema objectSchema = new ObjectBeanHelper().createObjetSchema();

        ObjectBean sourceBean = new ObjectBean(objectSchema);
        setProperty(sourceBean, "string", "a");
        setProperty(sourceBean, "number", 10);
        setProperty(sourceBean, "decimal", BigDecimal.TEN);
        setProperty(sourceBean, "date", today());
        setProperty(sourceBean, "time", now());
        setProperty(sourceBean, "boolean", Boolean.TRUE);
        setProperty(sourceBean, "binary", new Binary());
//        setProperty(sourceBean, "reference", ObjectReference.of("1", "Reference"));

        setProperty(sourceBean, "nested.string", "a");
        setProperty(sourceBean, "nested.number", 10);
        setProperty(sourceBean, "nested.decimal", BigDecimal.TEN);
        setProperty(sourceBean, "nested.date", today());
        setProperty(sourceBean, "nested.time", now());
        setProperty(sourceBean, "nested.boolean", Boolean.TRUE);
        setProperty(sourceBean, "nested.binary", new Binary());
//        setProperty(sourceBean, "nested.reference", ObjectReference.of("1", "Reference"));
        setProperty(sourceBean, "nested.nested.string", "a");
        setProperty(sourceBean, "nested.indexed[0].string", "a");

        setProperty(sourceBean, "indexed[0].string", "a");
        setProperty(sourceBean, "indexed[0].number", 10);
        setProperty(sourceBean, "indexed[0].decimal", BigDecimal.TEN);
        setProperty(sourceBean, "indexed[0].date", today());
        setProperty(sourceBean, "indexed[0].time", now());
        setProperty(sourceBean, "indexed[0].boolean", Boolean.TRUE);
        setProperty(sourceBean, "indexed[0].binary", new Binary());
//        setProperty(sourceBean, "indexed[0].reference", ObjectReference.of("1", "Reference"));
        setProperty(sourceBean, "indexed[0].nested.string", "a");
        setProperty(sourceBean, "indexed[0].indexed[0].string", "a");

        //when
        //initialize an new AtttributeStore with given values
        DynaBean resultBean = ObjectBean.from(objectSchema, sourceBean.toIdMap());

        //then
        assertGetProperty(resultBean, "string", "a");
        assertGetProperty(resultBean, "number", 10);
        assertGetProperty(resultBean, "decimal", BigDecimal.TEN);
        assertGetProperty(resultBean, "date", today());
        assertGetProperty(resultBean, "time", now());
        assertGetProperty(resultBean, "boolean", Boolean.TRUE);
        assertGetProperty(resultBean, "binary", is(notNullValue()));
//        assertGetProperty(resultBean, "reference", ObjectReference.of("1", "Reference"));

        assertGetProperty(resultBean, "nested.string", "a");
        assertGetProperty(resultBean, "nested.number", 10);
        assertGetProperty(resultBean, "nested.decimal", BigDecimal.TEN);
        assertGetProperty(resultBean, "nested.date", today());
        assertGetProperty(resultBean, "nested.time", now());
        assertGetProperty(resultBean, "nested.boolean", Boolean.TRUE);
        assertGetProperty(resultBean, "nested.binary", is(notNullValue()));
//        assertGetProperty(resultBean, "nested.reference", ObjectReference.of("1", "Reference"));
        assertGetProperty(resultBean, "nested.nested.string", "a");
//TODO        assertGetProperty(resultBean, "nested.indexed[0].string","a"));

        assertGetProperty(resultBean, "indexed[0].string", "a");
        assertGetProperty(resultBean, "indexed[0].number", 10);
        assertGetProperty(resultBean, "indexed[0].decimal", BigDecimal.TEN);
        assertGetProperty(resultBean, "indexed[0].date", today());
        assertGetProperty(resultBean, "indexed[0].time", now());
        assertGetProperty(resultBean, "indexed[0].boolean", Boolean.TRUE);
        assertGetProperty(resultBean, "indexed[0].binary", is(notNullValue()));
//        assertGetProperty(resultBean, "indexed[0].reference", ObjectReference.of("1", "Reference"));
        assertGetProperty(resultBean, "indexed[0].nested.string", "a");
        assertGetProperty(resultBean, "indexed[0].indexed[0].string", "a");

    }

    private void assertSetGetProperty(DynaBean dynaBean, String expression, Object value) throws ReflectiveOperationException {
        setProperty(dynaBean, expression, value);

        assertGetProperty(dynaBean, expression, value);
    }

    private void assertGetProperty(DynaBean dynaBean, String expression, Object value) throws ReflectiveOperationException {
        assertGetProperty(dynaBean, expression, is(value));
    }

    private void assertGetProperty(DynaBean dynaBean, String expression, Matcher<Object> matcher) throws ReflectiveOperationException {
        Object propertyValue = getProperty(dynaBean, expression);
        assertThat("value of '" + expression + "' must be " + matcher, propertyValue, matcher);
    }
}
