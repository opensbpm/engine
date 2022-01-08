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

import org.opensbpm.engine.core.utils.entities.HasId;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class ReferenceAttributeModel extends AttributeModel implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reference", updatable = false)
    private ObjectModel reference;

    protected ReferenceAttributeModel() {
        //JAXB constructor
    }

    public ReferenceAttributeModel(ObjectModel objectModel, String name, ObjectModel reference) {
        super(objectModel, name);
        this.reference = Objects.requireNonNull(reference);
    }

    public ReferenceAttributeModel(AttributeModel parent, String name, ObjectModel reference) {
        super(parent, name);
        this.reference = Objects.requireNonNull(reference);
    }

    @Override
    public Long getId() {
        return id;
    }

    public ObjectModel getReference() {
        return reference;
    }

    @Override
    public <T> T accept(AttributeModelVisitor<T> visitor) {
        return visitor.visitReference(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("refernce", reference)
                .toString();
    }
}
