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
package org.opensbpm.engine.core.engine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanPropertyMapDecorator;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.model.Binary;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ObjectReference;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ObjectBeanTest extends ServiceITCase {

    private ScriptEngine scriptEngine;

    @Autowired
    private ProcessModelService processModelService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        scriptEngine = new ScriptEngineManager().getEngineByMimeType("application/x-groovy");
    }

    private ObjectModel createObjetModel() {
        ObjectModel refObjectModel = new ObjectModel("ref");
        refObjectModel.addAttributeModel(new SimpleAttributeModel(refObjectModel, "name", FieldType.STRING));

        ObjectModel objectModel = new ObjectModel("root");
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "string", FieldType.STRING));
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "number", FieldType.NUMBER));
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "decimal", FieldType.DECIMAL));
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "date", FieldType.DATE));
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "time", FieldType.TIME));
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "boolean", FieldType.BOOLEAN));
        objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "binary", FieldType.BINARY));
        objectModel.addAttributeModel(new ReferenceAttributeModel(objectModel, "reference", refObjectModel));

        NestedAttributeModel nestedModel = objectModel.addAttributeModel(new NestedAttributeModel(objectModel, "nested"));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "string", FieldType.STRING));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "number", FieldType.NUMBER));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "decimal", FieldType.DECIMAL));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "date", FieldType.DATE));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "time", FieldType.TIME));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "boolean", FieldType.BOOLEAN));
        nestedModel.addAttributeModel(new SimpleAttributeModel(nestedModel, "binary", FieldType.BINARY));
        nestedModel.addAttributeModel(new ReferenceAttributeModel(nestedModel, "reference", refObjectModel));
        NestedAttributeModel nestedNestedModel = nestedModel.addAttributeModel(new NestedAttributeModel(nestedModel, "nested"));
        nestedNestedModel.addAttributeModel(new SimpleAttributeModel(nestedNestedModel, "string", FieldType.STRING));
        NestedAttributeModel indexedNestedModel = nestedModel.addAttributeModel(new IndexedAttributeModel(nestedModel, "indexed"));
        indexedNestedModel.addAttributeModel(new SimpleAttributeModel(indexedNestedModel, "string", FieldType.STRING));

        NestedAttributeModel indexedModel = objectModel.addAttributeModel(new IndexedAttributeModel(objectModel, "indexed"));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "string", FieldType.STRING));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "number", FieldType.NUMBER));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "decimal", FieldType.DECIMAL));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "date", FieldType.DATE));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "time", FieldType.TIME));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "boolean", FieldType.BOOLEAN));
        indexedModel.addAttributeModel(new SimpleAttributeModel(indexedModel, "binary", FieldType.BINARY));
        indexedModel.addAttributeModel(new ReferenceAttributeModel(indexedModel, "reference", refObjectModel));
        NestedAttributeModel nestedIndexedModel = indexedModel.addAttributeModel(new NestedAttributeModel(indexedModel, "nested"));
        nestedIndexedModel.addAttributeModel(new SimpleAttributeModel(nestedIndexedModel, "string", FieldType.STRING));
        NestedAttributeModel indexedIndexedModel = indexedModel.addAttributeModel(new IndexedAttributeModel(indexedModel, "indexed"));
        indexedIndexedModel.addAttributeModel(new SimpleAttributeModel(indexedIndexedModel, "string", FieldType.STRING));

        return doInTransaction(() -> {
            ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
            processModel.addObjectModel(refObjectModel);
            processModel.addObjectModel(objectModel);
            processModelService.save(processModel);
            return objectModel;
        });
    }

    @Test
    public void testFieldTypes() throws Exception {
        ObjectModel objectModel = createObjetModel();

        DynaBean dynaBean = new ObjectBean(objectModel, new AttributeStore(objectModel));
        assertSetGetProperty(dynaBean, "string", "a");
        assertSetGetProperty(dynaBean, "number", 10);
        assertSetGetProperty(dynaBean, "decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "date", LocalDate.now());
        assertSetGetProperty(dynaBean, "time", LocalTime.now());
        assertSetGetProperty(dynaBean, "boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "binary", new Binary());
        assertSetGetProperty(dynaBean, "reference", ObjectReference.of("1", "Reference"));

        assertSetGetProperty(dynaBean, "nested.string", "a");
        assertSetGetProperty(dynaBean, "nested.number", 10);
        assertSetGetProperty(dynaBean, "nested.decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "nested.date", LocalDate.now());
        assertSetGetProperty(dynaBean, "nested.time", LocalTime.now());
        assertSetGetProperty(dynaBean, "nested.boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "nested.binary", new Binary());
        assertSetGetProperty(dynaBean, "nested.reference", ObjectReference.of("1", "Reference"));
        assertSetGetProperty(dynaBean, "nested.nested.string", "a");
        assertSetGetProperty(dynaBean, "nested.indexed[0].string", "a");

        assertSetGetProperty(dynaBean, "indexed[0].string", "a");
        assertSetGetProperty(dynaBean, "indexed[0].number", 10);
        assertSetGetProperty(dynaBean, "indexed[0].decimal", BigDecimal.TEN);
        assertSetGetProperty(dynaBean, "indexed[0].date", LocalDate.now());
        assertSetGetProperty(dynaBean, "indexed[0].time", LocalTime.now());
        assertSetGetProperty(dynaBean, "indexed[0].boolean", Boolean.TRUE);
        assertSetGetProperty(dynaBean, "indexed[0].binary", new Binary());
        assertSetGetProperty(dynaBean, "indexed[0].reference", ObjectReference.of("1", "Reference"));
        assertSetGetProperty(dynaBean, "indexed[0].nested.string", "a");
        assertSetGetProperty(dynaBean, "indexed[0].indexed[0].string", "a");

        @SuppressWarnings("unchecked")
        List<ObjectBean> indexedList = (List<ObjectBean>) PropertyUtils.getProperty(dynaBean, "indexed");
        assertThat(indexedList, hasSize(1));

    }

    @Test
    public void testNestedTypes() throws Exception {
        //arrange
        ObjectModel objectModel = createObjetModel();

        DynaBean dynaBean = new ObjectBean(objectModel, new AttributeStore(objectModel));

        //act + assert
        assertThat(PropertyUtils.getProperty(dynaBean, "nested"), is(instanceOf(ObjectBean.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "nested.nested"), is(instanceOf(ObjectBean.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "nested.indexed"), is(instanceOf(List.class)));

        assertThat(PropertyUtils.getProperty(dynaBean, "indexed"), is(instanceOf(List.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "indexed[0].nested"), is(instanceOf(ObjectBean.class)));
        assertThat(PropertyUtils.getProperty(dynaBean, "indexed[0].indexed"), is(instanceOf(List.class)));
    }

    @Test
    public void testGivenValues() throws Exception {
        //arrange
        ObjectModel objectModel = createObjetModel();
        
        ObjectInstance objectInstance = new ObjectInstance(objectModel, new ProcessInstance() {
        });
        ObjectBean objectBean = new ObjectBean(objectModel, objectInstance.getAttributeStore());
        
        PropertyUtils.setProperty(objectBean, "string", "a");
        PropertyUtils.setProperty(objectBean, "number", 10);
        PropertyUtils.setProperty(objectBean, "decimal", BigDecimal.TEN);
        PropertyUtils.setProperty(objectBean, "date", LocalDate.now());
        PropertyUtils.setProperty(objectBean, "time", LocalTime.now());
        PropertyUtils.setProperty(objectBean, "boolean", Boolean.TRUE);
        PropertyUtils.setProperty(objectBean, "binary", new Binary());
        PropertyUtils.setProperty(objectBean, "reference", ObjectReference.of("1", "Reference"));

        PropertyUtils.setProperty(objectBean, "nested.string", "a");
        PropertyUtils.setProperty(objectBean, "nested.number", 10);
        PropertyUtils.setProperty(objectBean, "nested.decimal", BigDecimal.TEN);
        PropertyUtils.setProperty(objectBean, "nested.date", LocalDate.now());
        PropertyUtils.setProperty(objectBean, "nested.time", LocalTime.now());
        PropertyUtils.setProperty(objectBean, "nested.boolean", Boolean.TRUE);
        PropertyUtils.setProperty(objectBean, "nested.binary", new Binary());
        PropertyUtils.setProperty(objectBean, "nested.reference", ObjectReference.of("1", "Reference"));
        PropertyUtils.setProperty(objectBean, "nested.nested.string", "a");
        PropertyUtils.setProperty(objectBean, "nested.indexed[0].string", "a");

        PropertyUtils.setProperty(objectBean, "indexed[0].string", "a");
        PropertyUtils.setProperty(objectBean, "indexed[0].number", 10);
        PropertyUtils.setProperty(objectBean, "indexed[0].decimal", BigDecimal.TEN);
        PropertyUtils.setProperty(objectBean, "indexed[0].date", LocalDate.now());
        PropertyUtils.setProperty(objectBean, "indexed[0].time", LocalTime.now());
        PropertyUtils.setProperty(objectBean, "indexed[0].boolean", Boolean.TRUE);
        PropertyUtils.setProperty(objectBean, "indexed[0].binary", new Binary());
        PropertyUtils.setProperty(objectBean, "indexed[0].reference", ObjectReference.of("1", "Reference"));
        PropertyUtils.setProperty(objectBean, "indexed[0].nested.string", "a");
        PropertyUtils.setProperty(objectBean, "indexed[0].indexed[0].string", "a");

        //act + assert
        DynaBean dynaBean = objectInstance.getObjectBean();

        assertNotNullProperty(dynaBean, "string");
        assertNotNullProperty(dynaBean, "number");
        assertNotNullProperty(dynaBean, "decimal");
        assertNotNullProperty(dynaBean, "date");
        assertNotNullProperty(dynaBean, "time");
        assertNotNullProperty(dynaBean, "boolean");
        assertNotNullProperty(dynaBean, "binary");
        assertNotNullProperty(dynaBean, "reference");
        assertNotNullProperty(dynaBean, "reference");

        assertNotNullProperty(dynaBean, "nested.string");
        assertNotNullProperty(dynaBean, "nested.number");
        assertNotNullProperty(dynaBean, "nested.decimal");
        assertNotNullProperty(dynaBean, "nested.date");
        assertNotNullProperty(dynaBean, "nested.time");
        assertNotNullProperty(dynaBean, "nested.boolean");
        assertNotNullProperty(dynaBean, "nested.binary");
        assertNotNullProperty(dynaBean, "nested.reference");
        assertNotNullProperty(dynaBean, "nested.reference");
        assertNotNullProperty(dynaBean, "nested.nested.string");
        assertNotNullProperty(dynaBean, "nested.indexed[0].string");

        assertNotNullProperty(dynaBean, "indexed[0].string");
        assertNotNullProperty(dynaBean, "indexed[0].number");
        assertNotNullProperty(dynaBean, "indexed[0].decimal");
        assertNotNullProperty(dynaBean, "indexed[0].date");
        assertNotNullProperty(dynaBean, "indexed[0].time");
        assertNotNullProperty(dynaBean, "indexed[0].boolean");
        assertNotNullProperty(dynaBean, "indexed[0].binary");
        assertNotNullProperty(dynaBean, "indexed[0].reference");
        assertNotNullProperty(dynaBean, "indexed[0].reference");
        assertNotNullProperty(dynaBean, "indexed[0].nested.string");
        assertNotNullProperty(dynaBean, "indexed[0].indexed[0].string");
    }

    private void assertSetGetProperty(DynaBean dynaBean, String expression, Object value) throws ReflectiveOperationException, ScriptException {
        PropertyUtils.setProperty(dynaBean, expression, value);

        Object propertyValue = PropertyUtils.getProperty(dynaBean, expression);
        assertThat("value of '" + expression + "' must be " + value, propertyValue, is(value));

        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("root", new DynaBeanPropertyMapDecorator(dynaBean));
        Object groovyValue = scriptEngine.eval("root." + expression, bindings);
        assertThat("groovyValue of 'root." + expression + "' must be " + value, groovyValue, is(value));
    }

    private void assertNotNullProperty(DynaBean dynaBean, String expression) throws ReflectiveOperationException, ScriptException {
        Object propertyValue = PropertyUtils.getProperty(dynaBean, expression);
        assertThat("value of '" + expression + "' must be non null", propertyValue, is(notNullValue()));
    }
}
