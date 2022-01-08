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
import java.util.Arrays;
import java.util.List;
import javax.script.ScriptException;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import org.opensbpm.engine.api.model.Binary;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.Occurs;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class ObjectBeanTest {

    private ObjectSchema createObjetSchema() {
        ObjectSchema refObjectSchema = ObjectSchema.of(id(), "ref", Arrays.asList(
                new /*Simple*/ AttributeSchema(id(), "name", FieldType.STRING)
        ));

        return ObjectSchema.of(id(), "root", Arrays.asList(
                new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING),
                new /*Simple*/ AttributeSchema(id(), "number", FieldType.NUMBER),
                new /*Simple*/ AttributeSchema(id(), "decimal", FieldType.DECIMAL),
                new /*Simple*/ AttributeSchema(id(), "date", FieldType.DATE),
                new /*Simple*/ AttributeSchema(id(), "time", FieldType.TIME),
                new /*Simple*/ AttributeSchema(id(), "boolean", FieldType.BOOLEAN),
                new /*Simple*/ AttributeSchema(id(), "binary", FieldType.BINARY),
                //new ReferenceAttributeSchema(id(), "reference", refObjectSchema)
                new NestedAttributeSchema(id(), "nested", Occurs.ONE, Arrays.asList(
                        new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING),
                        new /*Simple*/ AttributeSchema(id(), "number", FieldType.NUMBER),
                        new /*Simple*/ AttributeSchema(id(), "decimal", FieldType.DECIMAL),
                        new /*Simple*/ AttributeSchema(id(), "date", FieldType.DATE),
                        new /*Simple*/ AttributeSchema(id(), "time", FieldType.TIME),
                        new /*Simple*/ AttributeSchema(id(), "boolean", FieldType.BOOLEAN),
                        new /*Simple*/ AttributeSchema(id(), "binary", FieldType.BINARY),
                        //new ReferenceAttributeSchema(id(), "reference", refObjectSchema),
                        new NestedAttributeSchema(id(), "nested", Occurs.ONE, Arrays.asList(
                                new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING)
                        )),
                        new /*Indexed*/ NestedAttributeSchema(id(), "indexed", Occurs.UNBOUND, Arrays.asList(
                                new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING)
                        ))
                )),
                new NestedAttributeSchema(id(), "indexed", Occurs.UNBOUND, Arrays.asList(
                        new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING),
                        new /*Simple*/ AttributeSchema(id(), "number", FieldType.NUMBER),
                        new /*Simple*/ AttributeSchema(id(), "decimal", FieldType.DECIMAL),
                        new /*Simple*/ AttributeSchema(id(), "date", FieldType.DATE),
                        new /*Simple*/ AttributeSchema(id(), "time", FieldType.TIME),
                        new /*Simple*/ AttributeSchema(id(), "boolean", FieldType.BOOLEAN),
                        new /*Simple*/ AttributeSchema(id(), "binary", FieldType.BINARY),
                        //new ReferenceAttributeSchema(id(), "reference", refObjectSchema),
                        new NestedAttributeSchema(id(), "nested", Occurs.ONE, Arrays.asList(
                                new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING)
                        )),
                        new /*Indexed*/ NestedAttributeSchema(id(), "indexed", Occurs.UNBOUND, Arrays.asList(
                                new /*Simple*/ AttributeSchema(id(), "string", FieldType.STRING)
                        ))
                ))
        ));
    }

    private long id = 1l;

    private long id() {
        return id++;
    }

    @Test
    public void testFieldTypes() throws Exception {
        ObjectSchema objectSchema = createObjetSchema();

        DynaBean dynaBean = new ObjectBean(objectSchema, new AttributeStore(objectSchema));
        assertSetGetProperty(dynaBean, "string", "a");
        assertSetGetProperty(dynaBean, "number", 10);
        assertSetGetProperty(dynaBean, "decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "date", LocalDate.now());
        assertSetGetProperty(dynaBean, "time", LocalTime.now());
        assertSetGetProperty(dynaBean, "boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "binary", new Binary());
        //assertSetGetProperty(dynaBean, "reference", ObjectReference.of("1", "Reference"));

        assertSetGetProperty(dynaBean, "nested.string", "a");
        assertSetGetProperty(dynaBean, "nested.number", 10);
        assertSetGetProperty(dynaBean, "nested.decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "nested.date", LocalDate.now());
        assertSetGetProperty(dynaBean, "nested.time", LocalTime.now());
        assertSetGetProperty(dynaBean, "nested.boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "nested.binary", new Binary());
        //assertSetGetProperty(dynaBean, "nested.reference", ObjectReference.of("1", "Reference"));
        assertSetGetProperty(dynaBean, "nested.nested.string", "a");
        assertSetGetProperty(dynaBean, "nested.indexed[0].string", "a");

        assertSetGetProperty(dynaBean, "indexed[0].string", "a");
        assertSetGetProperty(dynaBean, "indexed[0].number", 10);
        assertSetGetProperty(dynaBean, "indexed[0].decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "indexed[0].date", LocalDate.now());
        assertSetGetProperty(dynaBean, "indexed[0].time", LocalTime.now());
        assertSetGetProperty(dynaBean, "indexed[0].boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "indexed[0].binary", new Binary());
        //assertSetGetProperty(dynaBean, "indexed[0].reference", ObjectReference.of("1", "Reference"));
        assertSetGetProperty(dynaBean, "indexed[0].nested.string", "a");
        assertSetGetProperty(dynaBean, "indexed[0].indexed[0].string", "a");

        @SuppressWarnings("unchecked")
        List<ObjectBean> indexedList = (List<ObjectBean>) PropertyUtils.getProperty(dynaBean, "indexed");
        assertThat(indexedList, hasSize(1));

    }

    @Test
    public void testNestedTypes() throws Exception {
        ObjectSchema objectSchema = createObjetSchema();

        DynaBean dynaBean = new ObjectBean(objectSchema, new AttributeStore(objectSchema));

        assertThat(PropertyUtils.getProperty(dynaBean, "nested"), is(instanceOf(ObjectBean.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "nested.nested"), is(instanceOf(ObjectBean.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "nested.indexed"), is(instanceOf(List.class)));

        assertThat(PropertyUtils.getProperty(dynaBean, "indexed"), is(instanceOf(List.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "indexed[0].nested"), is(instanceOf(ObjectBean.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "indexed[0].indexed"), is(instanceOf(List.class)));
    }

    @Test
    public void testGivenValues() throws Exception {
        //given

        ObjectSchema objectSchema = createObjetSchema();
        AttributeStore sourceStore = new AttributeStore(objectSchema);

        ObjectBean sourceBean = new ObjectBean(objectSchema, sourceStore);
        PropertyUtils.setProperty(sourceBean, "string", "a");
        PropertyUtils.setProperty(sourceBean, "number", 10);
        PropertyUtils.setProperty(sourceBean, "decimal", BigDecimal.TEN);
        PropertyUtils.setProperty(sourceBean, "date", LocalDate.now());
        PropertyUtils.setProperty(sourceBean, "time", LocalTime.now());
        PropertyUtils.setProperty(sourceBean, "boolean", Boolean.TRUE);
        PropertyUtils.setProperty(sourceBean, "binary", new Binary());
//        PropertyUtils.setProperty(sourceBean, "reference", ObjectReference.of("1", "Reference"));

        PropertyUtils.setProperty(sourceBean, "nested.string", "a");
        PropertyUtils.setProperty(sourceBean, "nested.number", 10);
        PropertyUtils.setProperty(sourceBean, "nested.decimal", BigDecimal.TEN);
        PropertyUtils.setProperty(sourceBean, "nested.date", LocalDate.now());
        PropertyUtils.setProperty(sourceBean, "nested.time", LocalTime.now());
        PropertyUtils.setProperty(sourceBean, "nested.boolean", Boolean.TRUE);
        PropertyUtils.setProperty(sourceBean, "nested.binary", new Binary());
//        PropertyUtils.setProperty(sourceBean, "nested.reference", ObjectReference.of("1", "Reference"));
        PropertyUtils.setProperty(sourceBean, "nested.nested.string", "a");
        PropertyUtils.setProperty(sourceBean, "nested.indexed[0].string", "a");

        PropertyUtils.setProperty(sourceBean, "indexed[0].string", "a");
        PropertyUtils.setProperty(sourceBean, "indexed[0].number", 10);
        PropertyUtils.setProperty(sourceBean, "indexed[0].decimal", BigDecimal.TEN);
        PropertyUtils.setProperty(sourceBean, "indexed[0].date", LocalDate.now());
        PropertyUtils.setProperty(sourceBean, "indexed[0].time", LocalTime.now());
        PropertyUtils.setProperty(sourceBean, "indexed[0].boolean", Boolean.TRUE);
        PropertyUtils.setProperty(sourceBean, "indexed[0].binary", new Binary());
//        PropertyUtils.setProperty(sourceBean, "indexed[0].reference", ObjectReference.of("1", "Reference"));
        PropertyUtils.setProperty(sourceBean, "indexed[0].nested.string", "a");
        PropertyUtils.setProperty(sourceBean, "indexed[0].indexed[0].string", "a");

        //when
        //initialize an new AtttributeStore with given values
        AttributeStore attributeStore = new AttributeStore(objectSchema, sourceStore.getValues());
        DynaBean dynaBean = new ObjectBean(objectSchema, attributeStore);

        //then
        assertNotNullProperty(dynaBean, "string");
        assertNotNullProperty(dynaBean, "number");
        assertNotNullProperty(dynaBean, "decimal");
        assertNotNullProperty(dynaBean, "date");
        assertNotNullProperty(dynaBean, "time");
        assertNotNullProperty(dynaBean, "boolean");
        assertNotNullProperty(dynaBean, "binary");
//        assertNotNullProperty(dynaBean, "reference");
//        assertNotNullProperty(dynaBean, "reference");

        assertNotNullProperty(dynaBean, "nested.string");
        assertNotNullProperty(dynaBean, "nested.number");
        assertNotNullProperty(dynaBean, "nested.decimal");
        assertNotNullProperty(dynaBean, "nested.date");
        assertNotNullProperty(dynaBean, "nested.time");
        assertNotNullProperty(dynaBean, "nested.boolean");
        assertNotNullProperty(dynaBean, "nested.binary");
//        assertNotNullProperty(dynaBean, "nested.reference");
//        assertNotNullProperty(dynaBean, "nested.reference");
        assertNotNullProperty(dynaBean, "nested.nested.string");
        assertNotNullProperty(dynaBean, "nested.indexed[0].string");

        assertNotNullProperty(dynaBean, "indexed[0].string");
        assertNotNullProperty(dynaBean, "indexed[0].number");
        assertNotNullProperty(dynaBean, "indexed[0].decimal");
        assertNotNullProperty(dynaBean, "indexed[0].date");
        assertNotNullProperty(dynaBean, "indexed[0].time");
        assertNotNullProperty(dynaBean, "indexed[0].boolean");
        assertNotNullProperty(dynaBean, "indexed[0].binary");
//        assertNotNullProperty(dynaBean, "indexed[0].reference");
//        assertNotNullProperty(dynaBean, "indexed[0].reference");
        assertNotNullProperty(dynaBean, "indexed[0].nested.string");
        assertNotNullProperty(dynaBean, "indexed[0].indexed[0].string");
    }

    private void assertSetGetProperty(DynaBean dynaBean, String expression, Object value) throws ReflectiveOperationException, ScriptException {
        PropertyUtils.setProperty(dynaBean, expression, value);

        Object propertyValue = PropertyUtils.getProperty(dynaBean, expression);
        assertThat("value of '" + expression + "' must be " + value, propertyValue, is(value));

    }

    private void assertNotNullProperty(DynaBean dynaBean, String expression) throws ReflectiveOperationException, ScriptException {
        Object propertyValue = PropertyUtils.getProperty(dynaBean, expression);
        assertThat("value of '" + expression + "' must be non null", propertyValue, is(notNullValue()));
    }
}
