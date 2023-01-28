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
 * ****************************************************************************
 */
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.FieldType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleAttributeSchema extends AbstractAttributeSchema implements Serializable {

    public static final String ID_NAME = "Id";

    public static SimpleAttributeSchema of(long id, String name, FieldType fieldType) {
        return new SimpleAttributeSchema(id, name, fieldType);
    }

    @XmlAttribute(required = true)
    private FieldType fieldType;

    protected SimpleAttributeSchema() {
        //JAXB constructor
    }

    public SimpleAttributeSchema(Long id, String name, FieldType fieldType) {
        super(id, name);
        this.fieldType = Objects.requireNonNull(fieldType, "fieldType must not be null");
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * returns the java-type of the field.
     *
     * @return
     * @see FieldType
     */
    public Class<?> getType() {
        return fieldType.getType();
    }

    public <T> T accept(AttributeSchemaVisitor<T> visitor) {
        return visitor.visitSimple(this);
    }

    public boolean isIdSchema() {
        return ID_NAME.equals(getName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("type", fieldType)
                .toString();
    }
}
