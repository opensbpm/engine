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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.hasDatas;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.hasSchemas;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isFieldSchema;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isIndexedSchema;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isNestedSchema;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isObjectData;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isObjectSchema;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isReferenceSchema;

public class TaskResponseTest {

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
                        SimpleAttributeSchema.of(o1NumberFieldId, "Number Field", FieldType.NUMBER)
                ))
        ));
        ObjectSchema object2Schema = ObjectSchema.of(2l, "Object 2", asList(
                attributeSchema(o2StringFieldId, "String Field", FieldType.STRING, true, false, asList("1", "2")),
                referenceSchema(27L, "Reference Field", true, false, object1Schema),
                IndexedAttributeSchema.create(o2ToManyFieldId, "To Many", asList(
                        SimpleAttributeSchema.of(o2ManyNumberField, "Number Field", FieldType.NUMBER),
                        NestedAttributeSchema.createNested(o2ManyOneFieldId, "To One", asList(
                                SimpleAttributeSchema.of(o2ManyOneNumber, "Number Field", FieldType.NUMBER)
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

        TaskResponse taskResponse = TaskResponse.of(Long.MIN_VALUE,
                asList(NextState.of(Long.MIN_VALUE, "next state")),
                LocalDateTime.MIN,
                asList(object1Schema, object2Schema), asList(objectData));

        //when
        TaskResponse result = DeserializerUtil.deserializeJaxb(TaskResponse.class, taskResponse);

        //then
        assertThat("wrong id", result.getId(), is(taskResponse.getId()));
        assertThat("wrong states", result.getNextStates(), is(taskResponse.getNextStates()));
        assertThat("wrong lastChanged", result.getLastChanged(), is(taskResponse.getLastChanged()));

        assertThat("wrong document-schemas", result, hasSchemas(
                isObject1(),
                isObjectSchema("Object 2",
                        isFieldSchema("String Field", FieldType.STRING, true, false),
                        isReferenceSchema("Reference Field", true, false,
                                isObject1()
                        ),
                        isIndexedSchema("To Many",
                                isFieldSchema("Number Field", FieldType.NUMBER),
                                isNestedSchema("To One",
                                        isFieldSchema("Number Field", FieldType.NUMBER)
                                )
                        )
                )
        ));
        assertThat("wrong document-data", result, hasDatas(
                isObjectData("Object 1", "Object 1 Display",
                        hasEntry(o1StringFieldId, "A String")
                //                        ,hasEntry(o1ToOneFieldId,
                //                                hasEntry(o1NumberFieldId, 1)
                //                        )
                )
        ));
    }

    private static Matcher<ObjectSchema> isObject1() {
        return isObjectSchema("Object 1",
                isFieldSchema("String Field", FieldType.STRING, true, false),
                isNestedSchema("To One",
                        isFieldSchema("Number Field", FieldType.NUMBER)
                )
        );
    }

    private static SimpleAttributeSchema attributeSchema(Long id, String name, FieldType type, boolean required, boolean readOnly) {
        SimpleAttributeSchema attributeSchema = SimpleAttributeSchema.of(id, name, type);
        attributeSchema.setRequired(required);
        attributeSchema.setReadonly(readOnly);
        return attributeSchema;
    }

<<<<<<< HEAD (084f9ca) - activate junit + jacoco
    private static SimpleAttributeSchema referenceSchema(Long id, String name, FieldType type, boolean required, boolean readOnly, ObjectSchema autocompleteReference) {
        SimpleAttributeSchema attributeSchema = SimpleAttributeSchema.ofReference(id, name, autocompleteReference);
        attributeSchema.setRequired(required);
        attributeSchema.setReadonly(readOnly);
        return attributeSchema;
    }
    
    private static ReferenceAttributeSchema referenceSchema(Long id, String name, boolean required, boolean readOnly, ObjectSchema autocompleteReference, List<AttributeSchema> attributes) {
        ReferenceAttributeSchema attributeSchema = ReferenceAttributeSchema.create(id, name, attributes);
        attributeSchema.setAutocompleteReference(autocompleteReference);
=======
    private static ReferenceAttributeSchema referenceSchema(Long id, String name, boolean required, boolean readOnly, ObjectSchema autocompleteReference) {
        ReferenceAttributeSchema attributeSchema = ReferenceAttributeSchema.create(id, name, autocompleteReference);
>>>>>>> 3664284 (FieldType REFERENCE removed; it's a duplicate of ReferenceAttribute)
        attributeSchema.setRequired(required);
        attributeSchema.setReadonly(readOnly);
        return attributeSchema;
    }

}
