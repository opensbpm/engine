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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.Occurs;

@XmlAccessorType(XmlAccessType.FIELD)
public class NestedAttributeSchema extends AbstractContainerAttributeSchema {

    public static NestedAttributeSchema createNested(Long id, String name, List<AttributeSchema> attributes) {
        return new NestedAttributeSchema(id, name, Occurs.ONE, attributes);
    }

    public static NestedAttributeSchema createIndexed(Long id, String name, List<AttributeSchema> attributes) {
        return new IndexedAttributeSchema(id, name, attributes);
    }

    @XmlAttribute(required = true)
    private Occurs occurs;

    public NestedAttributeSchema() {
        //JAXB constructor
    }

    protected NestedAttributeSchema(Long id, String name, Occurs occurs, List<AttributeSchema> attributes) {
        super(id, name, occurs == Occurs.ONE ? FieldType.NESTED : FieldType.LIST, attributes);
        this.occurs = occurs;
    }

    public <T> T accept(AttributeSchemaVisitor<T> visitor) {
        if (Occurs.ONE == occurs) {
            return visitor.visitNested(this);
        }  else {
            throw new UnsupportedOperationException(occurs + " not implemented yet");
        }
    }

}
