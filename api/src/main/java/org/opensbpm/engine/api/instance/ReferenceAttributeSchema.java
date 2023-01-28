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
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceAttributeSchema extends AbstractAttributeSchema implements Serializable {

    public static ReferenceAttributeSchema create(Long id, String name, ObjectSchema autocompleteReference) {
        final ReferenceAttributeSchema attributeSchema = new ReferenceAttributeSchema(id, name);
        attributeSchema.autocompleteReference = autocompleteReference;
        return attributeSchema;
    }

    @XmlElement
    private ObjectSchema autocompleteReference;

    public ReferenceAttributeSchema() {
        //JAXB constructor
    }

    private ReferenceAttributeSchema(Long id, String name) {
        super(id, name);
    }

    public Optional<ObjectSchema> getAutocompleteReference() {
        return Optional.ofNullable(autocompleteReference);
    }

    @Override
    public <T> T accept(AttributeSchemaVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

}
