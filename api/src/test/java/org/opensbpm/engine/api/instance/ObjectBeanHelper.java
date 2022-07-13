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

import org.opensbpm.engine.api.junit.ObjectSchemaBuilder;
import org.opensbpm.engine.api.model.FieldType;
import static org.opensbpm.engine.api.junit.ObjectSchemaBuilder.indexed;
import static org.opensbpm.engine.api.junit.ObjectSchemaBuilder.nested;
import static org.opensbpm.engine.api.junit.ObjectSchemaBuilder.schema;
import static org.opensbpm.engine.api.junit.ObjectSchemaBuilder.simple;

public class ObjectBeanHelper {

    public static ObjectSchema createObjetSchema() {
        ObjectSchema refObjectSchema = ObjectSchemaBuilder.schema("ref")
                .attribute(simple("name", FieldType.STRING))
                .build();

        return schema("root")
                .attribute(simple("string", FieldType.STRING))
                .attribute(simple("number", FieldType.NUMBER))
                .attribute(simple("decimal", FieldType.DECIMAL))
                .attribute(simple("date", FieldType.DATE))
                .attribute(simple("time", FieldType.TIME))
                .attribute(simple("boolean", FieldType.BOOLEAN))
                .attribute(simple("binary", FieldType.BINARY))
                //.attribute(new ReferenceAttributeSchema(id(), "reference", refObjectSchema)
                .attribute(nested("nested")
                        .attribute(simple("string", FieldType.STRING))
                        .attribute(simple("number", FieldType.NUMBER))
                        .attribute(simple("decimal", FieldType.DECIMAL))
                        .attribute(simple("date", FieldType.DATE))
                        .attribute(simple("time", FieldType.TIME))
                        .attribute(simple("boolean", FieldType.BOOLEAN))
                        .attribute(simple("binary", FieldType.BINARY))
                        //.attribute(new ReferenceAttributeSchema(id(), "reference", refObjectSchema))
                        .attribute(nested("nested")
                                .attribute(simple("string", FieldType.STRING))
                        )
                        .attribute(indexed("indexed")
                                .attribute(simple("string", FieldType.STRING))
                        )
                )
                .attribute(indexed("indexed")
                        .attribute(simple("string", FieldType.STRING))
                        .attribute(simple("number", FieldType.NUMBER))
                            .attribute(                        simple("decimal", FieldType.DECIMAL))
                        .attribute(simple("date", FieldType.DATE))
                        .attribute(simple("time", FieldType.TIME))
                        .attribute(simple("boolean", FieldType.BOOLEAN))
                        .attribute(simple("binary", FieldType.BINARY))
                        //.attribute(new ReferenceAttributeSchema(id(), "reference", refObjectSchema))
                        .attribute(nested("nested")
                                .attribute(simple("string", FieldType.STRING))
                        )
                        .attribute(indexed("indexed")
                                .attribute(simple("string", FieldType.STRING))
                        )
                )
                .build();
    }

}
