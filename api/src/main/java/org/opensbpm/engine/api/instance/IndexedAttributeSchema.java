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
import org.opensbpm.engine.api.model.definition.Occurs;

@XmlAccessorType(value = XmlAccessType.FIELD)
class IndexedAttributeSchema extends NestedAttributeSchema {

    public IndexedAttributeSchema() {
        //JAXB constructor
    }

    public IndexedAttributeSchema(Long id, String name, List<AttributeSchema> attributes) {
        super(id, name, Occurs.UNBOUND, attributes);
    }

    @Override
    public <T> T accept(AttributeSchemaVisitor<T> visitor) {
        return visitor.visitIndexed(this);
    }

}
