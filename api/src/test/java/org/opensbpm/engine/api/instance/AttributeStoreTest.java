/** *****************************************************************************
 * Copyright (C) 2022 Stefan Sedelmaier
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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Map;
import org.junit.Test;
import org.opensbpm.engine.api.model.Binary;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static org.apache.commons.beanutils.PropertyUtils.setProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AttributeStoreTest {

    private static LocalDate today() {
        return LocalDate.of(2022, Month.JULY, 25);
    }

    private static LocalTime now() {
        return LocalTime.of(12, 34);
    }

    @Test
    public void testUpdateValues() throws Exception {
        //arrange
        ObjectSchema objectSchema = new ObjectBeanHelper().createObjetSchema();

        ObjectBean sourceBean = new ObjectBean(objectSchema);
        setProperty(sourceBean, "string", "a");
        setProperty(sourceBean, "number", 10);
        setProperty(sourceBean, "decimal", BigDecimal.TEN);
        setProperty(sourceBean, "date", today());
        setProperty(sourceBean, "time", now());
        setProperty(sourceBean, "boolean", Boolean.TRUE);
        setProperty(sourceBean, "binary", new Binary());

        setProperty(sourceBean, "nested.string", "a");
        setProperty(sourceBean, "nested.number", 10);
        setProperty(sourceBean, "nested.decimal", BigDecimal.TEN);
        setProperty(sourceBean, "nested.date", today());
        setProperty(sourceBean, "nested.time", now());
        setProperty(sourceBean, "nested.boolean", Boolean.TRUE);
        setProperty(sourceBean, "nested.binary", new Binary());
        setProperty(sourceBean, "nested.nested.string", "a");
        setProperty(sourceBean, "nested.indexed[0].string", "a");

        setProperty(sourceBean, "indexed[0].string", "a");
        setProperty(sourceBean, "indexed[0].number", 10);
        setProperty(sourceBean, "indexed[0].decimal", BigDecimal.TEN);
        setProperty(sourceBean, "indexed[0].date", today());
        setProperty(sourceBean, "indexed[0].time", now());
        setProperty(sourceBean, "indexed[0].boolean", Boolean.TRUE);
        setProperty(sourceBean, "indexed[0].binary", new Binary());
        setProperty(sourceBean, "indexed[0].nested.string", "a");
        setProperty(sourceBean, "indexed[0].indexed[0].string", "a");

        Map<Long, Serializable> values = sourceBean.toIdMap();

        AttributeStore attributeStore = new AttributeStore(objectSchema);

        //act
        attributeStore.updateValues(values);

        //assert
        ObjectBean resultBean = new ObjectBean(objectSchema, attributeStore);

        assertThat(getProperty(resultBean, "string"), is("a"));
        assertThat(getProperty(resultBean, "number"), is(10));
        assertThat(getProperty(resultBean, "decimal"), is(BigDecimal.TEN));
        assertThat(getProperty(resultBean, "date"), is(today()));
        assertThat(getProperty(resultBean, "time"), is(now()));
        assertThat(getProperty(resultBean, "boolean"), is(Boolean.TRUE));
        assertThat(getProperty(resultBean, "binary"), is(notNullValue()));

        assertThat(getProperty(resultBean, "nested.string"), is("a"));
        assertThat(getProperty(resultBean, "nested.number"), is(10));
        assertThat(getProperty(resultBean, "nested.decimal"), is(BigDecimal.TEN));
        assertThat(getProperty(resultBean, "nested.date"), is(today()));
        assertThat(getProperty(resultBean, "nested.time"), is(now()));
        assertThat(getProperty(resultBean, "nested.boolean"), is(Boolean.TRUE));
        assertThat(getProperty(resultBean, "nested.binary"), is(notNullValue()));
        assertThat(getProperty(resultBean, "nested.nested.string"), is("a"));
        assertThat(getProperty(resultBean, "nested.indexed[0].string"), is("a"));

        assertThat(getProperty(resultBean, "indexed[0].string"), is("a"));
        assertThat(getProperty(resultBean, "indexed[0].number"), is(10));
        assertThat(getProperty(resultBean, "indexed[0].decimal"), is(BigDecimal.TEN));
        assertThat(getProperty(resultBean, "indexed[0].date"), is(today()));
        assertThat(getProperty(resultBean, "indexed[0].time"), is(now()));
        assertThat(getProperty(resultBean, "indexed[0].boolean"), is(Boolean.TRUE));
        assertThat(getProperty(resultBean, "indexed[0].binary"), is(notNullValue()));
        assertThat(getProperty(resultBean, "indexed[0].nested.string"), is("a"));
        assertThat(getProperty(resultBean, "indexed[0].indexed[0].string"), is("a"));

    }

}
