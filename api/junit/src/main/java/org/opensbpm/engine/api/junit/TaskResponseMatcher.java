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
package org.opensbpm.engine.api.junit;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.hamcrest.Matcher;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.AbstractContainerAttributeSchema;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.SimpleAttributeSchema;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.model.FieldType;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.opensbpm.engine.api.junit.CommonMatchers.value;

/**
 * Factory for {@link TaskResponse} specific {{@link Matcher}'s
 */
public final class TaskResponseMatcher {

    public static Matcher<TaskResponse> hasSchemas(Matcher<ObjectSchema>... schemas) {
        return value(TaskResponse::getSchemas, contains(schemas));
    }

    /**
     * {@link Matcher} for {@link ObjectSchema#getName()} and {@link ObjectSchema#getAttributes()}
     *
     * @param name name to match
     * @return {@link Matcher} of type {@link ObjectSchema}
     */
    public static Matcher<ObjectSchema> isObjectSchema(String name, Matcher<AttributeSchema>... attributes) {
        return allOf(
                value(ObjectSchema::getName, is(name)),
                value(ObjectSchema::getAttributes, contains(attributes))
        );
    }

    public static Matcher<AttributeSchema> isFieldSchema(String name, FieldType fieldType) {
        return isFieldSchema(name, fieldType, false, false);
    }

    public static Matcher<AttributeSchema> isFieldSchema(String name, FieldType fieldType, boolean required, boolean readOnly) {
        return allOf(value(AttributeSchema.class, AttributeSchema::getName, is(name)),
                value(SimpleAttributeSchema.class, SimpleAttributeSchema::getFieldType, is(fieldType)),
                value(AttributeSchema.class, AttributeSchema::isRequired, is(required)),
                value(AttributeSchema.class, AttributeSchema::isReadonly, is(readOnly))
        );
    }

    /**
     * {@link Matcher} to match referencing attributes.
     * @param name name of the attribute schema
     * @param required <code>true</code> is the required flag must be set
     * @param readOnly <code>true</code> is the read only flag must be set
     * @param referenceObjectMatcher Matcher to match the referencing {@link ObjectSchema}
     * @return 
     */
    public static Matcher<AttributeSchema> isReferenceSchema(String name, boolean required, boolean readOnly, Matcher<ObjectSchema> referenceObjectMatcher) {
        return allOf(
                value(SimpleAttributeSchema.class, SimpleAttributeSchema::getName, is(name)),
                value(SimpleAttributeSchema.class, SimpleAttributeSchema::getFieldType, is(FieldType.REFERENCE)),
                value(AttributeSchema.class, AttributeSchema::isRequired, is(required)),
                value(AttributeSchema.class, AttributeSchema::isReadonly, is(readOnly)),
                value(SimpleAttributeSchema.class, schema -> schema.getAutocompleteReference().get(), referenceObjectMatcher)
        );
    }

    public static Matcher<AttributeSchema> isNestedSchema(String name, Matcher<AttributeSchema>... attributes) {
        return isContainerSchema(name, attributes);
    }

    public static Matcher<AttributeSchema> isIndexedSchema(String name, Matcher<AttributeSchema>... attributes) {
        return isContainerSchema(name, attributes);
    }

    private static Matcher<AttributeSchema> isContainerSchema(String name, Matcher<AttributeSchema>... attributes) {
        return allOf(
                value(AbstractContainerAttributeSchema.class, AbstractContainerAttributeSchema::getName, is(name)),
                value(AbstractContainerAttributeSchema.class, AbstractContainerAttributeSchema::getAttributes, contains(attributes))
        );
    }

    public static Matcher<TaskResponse> hasDatas(Matcher<ObjectData>... datas) {
        return value(TaskResponse::getDatas, contains(datas));
    }

    public static Matcher<ObjectData> isObjectData(String name, String displayName, Matcher<Map<?, ?>>... datas) {
        return allOf(
                value(ObjectData::getName, is(name)),
                value(ObjectData::getDisplayName, is(Optional.of(displayName))),
                value(ObjectData::getData, allOf(datas))
        );
    }

    private TaskResponseMatcher() {
    }

}
