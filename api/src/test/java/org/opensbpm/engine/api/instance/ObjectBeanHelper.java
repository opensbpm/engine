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

import java.util.Arrays;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.Occurs;

public class ObjectBeanHelper {

    public ObjectSchema createObjetSchema() {
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
    
    
}
