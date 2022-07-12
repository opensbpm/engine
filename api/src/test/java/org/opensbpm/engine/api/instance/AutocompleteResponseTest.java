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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import org.opensbpm.engine.api.instance.AutocompleteResponse.Autocomplete;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class AutocompleteResponseTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given
        Long o1StringFieldId = 1L;
        Long o1ToOneFieldId = 2L;
        Long o1NumberFieldId = 3L;

        Long o2StringFieldId = 4L;
        Long o2ToManyFieldId = 5L;
        Long o2ManyNumberField = 6L;
        Long o2ManyOneFieldId = 7L;
        Long o2ManyOneNumber = 8L;

        ObjectSchema object1Schema = ObjectSchema.of(1l, "Object 1", asList(
                attributeSchema(o1StringFieldId, "String Field", FieldType.STRING, true, false),
                NestedAttributeSchema.createNested(o1ToOneFieldId, "To One", asList(
                        new AttributeSchema(o1NumberFieldId, "Number Field", FieldType.NUMBER
                        )
                ))
        ));
        ObjectSchema object2Schema = ObjectSchema.of(2l, "Object 2", asList(
                attributeSchema(o2StringFieldId, "String Field", FieldType.STRING, true, false),
                IndexedAttributeSchema.create(o2ToManyFieldId, "To Many", asList(
                        new AttributeSchema(o2ManyNumberField, "Number Field", FieldType.NUMBER),
                        IndexedAttributeSchema.create(o2ManyOneFieldId, "To One", asList(
                                new AttributeSchema(o2ManyOneNumber, "Number Field", FieldType.NUMBER
                                )
                        ))
                ))
        ));

        Map<Long, Serializable> attributeData = new HashMap<>();
        attributeData.put(o1StringFieldId/*"String Field"*/, "A String");

        HashMap<Long, Serializable> nestedData = new HashMap<>();
        nestedData.put(o1NumberFieldId/*"Number Field"*/, 1);
        attributeData.put(o1ToOneFieldId/*"To One"*/, nestedData);

        ObjectData objectData = ObjectData.of("Object 1")
                .withDisplayName("Object 1 Display")
                .withData(attributeData)
                .build();

        AutocompleteResponse autocompleteResponse = AutocompleteResponse.of(asList(Autocomplete.of(objectData)));

        //when
        AutocompleteResponse result = DeserializerUtil.deserializeJaxb(AutocompleteResponse.class, autocompleteResponse);

        //then
        assertThat(result, notNullValue());
        assertThat(result.getAutocompletes(), hasSize(1));
        //TODO validate better
    }

    private static AttributeSchema attributeSchema(Long id, String name, FieldType type, boolean required, boolean readOnly) {
        AttributeSchema attributeSchema = new AttributeSchema(id, name, type);
        attributeSchema.setRequired(required);
        attributeSchema.setReadonly(readOnly);
        return attributeSchema;
    }

}
