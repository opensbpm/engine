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
package org.opensbpm.engine.core.model.entities;

import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.core.utils.entities.HasId;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class SimpleAttributeModel extends AttributeModel implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    private boolean indexed;

    @Column
    @Lob
    private Serializable defaultValue;

    protected SimpleAttributeModel() {
        //JAXB constructor
    }

    public SimpleAttributeModel(ObjectModel objectModel, String name, FieldType fieldType) {
        super(objectModel, name);
        this.fieldType = Objects.requireNonNull(fieldType);
    }

    public SimpleAttributeModel(AttributeModel parent, String name, FieldType fieldType) {
        super(parent, name);
        this.fieldType = Objects.requireNonNull(fieldType);
    }

    @Override
    public Long getId() {
        return id;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public Serializable getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Serializable defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public <T> T accept(AttributeModelVisitor<T> visitor) {
        return visitor.visitSimple(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("fieldType", fieldType)
                .toString();
    }
}
