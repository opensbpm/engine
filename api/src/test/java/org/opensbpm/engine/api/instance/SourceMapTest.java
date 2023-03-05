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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SourceMapTest {

    @Test
    public void test() {
        Map<String, Object> source = createSource();

        ObjectSchema object1Schema = ObjectSchema.of(1l, "Referenced", asList(
                SimpleAttributeSchema.of(1L, "Name", FieldType.STRING)
        ));
        
        ObjectSchema objectSchema = ObjectSchema.of(1l, "Test", asList(
                SimpleAttributeSchema.of(1L, "Name", FieldType.STRING),
                NestedAttributeSchema.createNested(3L, "Adresse", asList(
                        SimpleAttributeSchema.of(4L, "PLZ", FieldType.STRING),
                        SimpleAttributeSchema.of(5L, "Ort", FieldType.STRING)
                )),
                IndexedAttributeSchema.create(6L, "Kontakte", asList(
                        SimpleAttributeSchema.of(7L, "Name", FieldType.STRING),
                        SimpleAttributeSchema.of(8L, "EMail", FieldType.STRING)
                )),
                ReferenceAttributeSchema.create(2L, "Reference", object1Schema)
        ));

        Map<Long, Serializable> toIdMap = new SourceMap(objectSchema, source,null).toIdMap();
        assertThat(toIdMap, is(notNullValue()));
    }

    private Map<String, Object> createSource() {
        Map<String, Object> source = new HashMap<>();
        source.put("Name", "Hallo");
        
        Map<String, Object> address = new HashMap<>();
        address.put("PLZ", 1010);
        address.put("Ort", "Wien");
        source.put("Adresse", address);
        
        source.put("Kontakte", asList(
                createContact("Kontakt1", "mail@irgendwo.at"),
                createContact("Kontakt2", "mail@daham.at")
        ));
        source.put("Reference", asList(
                createContact("Kontakt1", "mail@irgendwo.at"),
                createContact("Kontakt2", "mail@daham.at")
        ));
        return source;
    }

    private Map<String, Object> createContact(String name, String mail) {
        Map<String, Object> map = new HashMap<>();
        map.put("Name", name);
        map.put("EMail", mail);
        return map;
    }

}
