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

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.core.utils.entities.HasId;

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
