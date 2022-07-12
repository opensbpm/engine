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

import java.util.List;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;

public class ObjectBeanHelper {

    public ObjectSchema createObjetSchema() {
        ObjectSchema refObjectSchema = schema("ref", asList(
                simple("name", FieldType.STRING)
        ));

        return schema("root", asList(
                simple("string", FieldType.STRING),
                simple("number", FieldType.NUMBER),
                simple("decimal", FieldType.DECIMAL),
                simple("date", FieldType.DATE),
                simple("time", FieldType.TIME),
                simple("boolean", FieldType.BOOLEAN),
                simple("binary", FieldType.BINARY),
                //new ReferenceAttributeSchema(id(), "reference", refObjectSchema)
                nested("nested", asList(
                        simple("string", FieldType.STRING),
                        simple("number", FieldType.NUMBER),
                        simple("decimal", FieldType.DECIMAL),
                        simple("date", FieldType.DATE),
                        simple("time", FieldType.TIME),
                        simple("boolean", FieldType.BOOLEAN),
                        simple("binary", FieldType.BINARY),
                        //new ReferenceAttributeSchema(id(), "reference", refObjectSchema),
                        nested("nested", asList(
                                simple("string", FieldType.STRING)
                        )),
                        indexed("indexed", asList(
                                simple("string", FieldType.STRING)
                        ))
                )),
                indexed("indexed", asList(
                        simple("string", FieldType.STRING),
                        simple("number", FieldType.NUMBER),
                        simple("decimal", FieldType.DECIMAL),
                        simple("date", FieldType.DATE),
                        simple("time", FieldType.TIME),
                        simple("boolean", FieldType.BOOLEAN),
                        simple("binary", FieldType.BINARY),
                        //new ReferenceAttributeSchema(id(), "reference", refObjectSchema),
                        nested("nested", asList(
                                simple("string", FieldType.STRING)
                        )),
                        indexed("indexed", asList(
                                simple("string", FieldType.STRING)
                        ))
                ))
        ));
    }

    public ObjectSchema schema(String name, AttributeSchema attributes) {
        return schema(name, asList(attributes));
    }

    public ObjectSchema schema(String name, List<AttributeSchema> attributes) {
        return ObjectSchema.of(id(), name, attributes);
    }

    public AttributeSchema simple(String name, FieldType type) {
        return new /*Simple*/ AttributeSchema(id(), name, type);
    }

    public AttributeSchema simpleRequired(String name, FieldType type) {
        return required(simple(name, type));
    }

    private AttributeSchema required(AttributeSchema attribute){
        attribute.setRequired(true);
        return attribute;
    }

    public AttributeSchema nested(String name, List<AttributeSchema> attributes) {
        return NestedAttributeSchema.createNested(id(), name, attributes);
    }

    public AttributeSchema indexed(String name, List<AttributeSchema> attributes) {
        return NestedAttributeSchema.createIndexed(id(), name, attributes);
    }

    private long id = 1l;

    private long id() {
        return id++;
    }

}
