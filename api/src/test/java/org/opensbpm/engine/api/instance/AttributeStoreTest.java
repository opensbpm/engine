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
import java.util.Arrays;
import java.util.HashMap;
import org.junit.Test;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.Occurs;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class AttributeStoreTest {

    @Test
    public void testUpdateValues() {
        //arrange
        long nameId = 1l;
        long nestedId = 2l;
        long nestedNameId = 3l;

        AttributeSchema nameSchema = new AttributeSchema(nameId, "name", FieldType.STRING);
        
        AttributeSchema nestedNameSchema = new AttributeSchema(nestedNameId, "nestedName", FieldType.STRING);
        AttributeSchema nestedSchema = new NestedAttributeSchema(nestedId, "nested", Occurs.ONE, singletonList(nestedNameSchema));
        
        ObjectSchema objectSchema = ObjectSchema.of(1l, "Object", Arrays.asList(nameSchema,nestedSchema));

        AttributeStore attributeStore = new AttributeStore(objectSchema);

        HashMap<Long, Serializable> values = new HashMap<>();
        values.put(nameId, "Name");
        
        HashMap<Long, Serializable> nestedValues = new HashMap<>();
        nestedValues.put(nestedNameId, "Nested Name");        
        values.put(nestedId, nestedValues);

        //act
        attributeStore.updateValues(values);

        //assert
        assertThat(attributeStore.getValues(), hasEntry(nameId, "Name"));
        
        HashMap<Long, Serializable> nestedResult = (HashMap<Long, Serializable>) attributeStore.getValues().get(nestedId);
        assertThat(nestedResult, hasEntry(nestedNameId, "Nested Name"));
    }

}
