/*******************************************************************************
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
 ******************************************************************************/
package org.opensbpm.engine.core.model.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.definition.Occurs;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity
//@Table(name = "tomanymodel", uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"name", "name"}),
//    @UniqueConstraint(columnNames = {"objectmodel", "position"})
//})
public class NestedAttributeModel extends AttributeModel implements IsAttributeParent, HasId, Serializable {

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Occurs occurs;

    @OneToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "attribute", nullable = false, updatable = false)
    @OrderBy("position ASC")
    private List<AttributeModel> attributeModels;

    protected NestedAttributeModel() {
        //JAXB constructor
    }

    protected NestedAttributeModel(ObjectModel objectModel, String name, Occurs occurs) {
        super(objectModel, name);
        this.occurs = occurs;
    }

    protected NestedAttributeModel(AttributeModel parent, String name, Occurs occurs) {
        super(parent, name);
        this.occurs = occurs;
    }

    public NestedAttributeModel(ObjectModel objectModel, String name) {
        this(objectModel, name, Occurs.ONE);
    }

    public NestedAttributeModel(AttributeModel parent, String name) {
        this(parent, name, Occurs.ONE);
    }

    public Occurs getOccurs() {
        return occurs;
    }

    @Override
    public Collection<AttributeModel> getAttributeModels() {
        return emptyOrUnmodifiableList(attributeModels);
    }

    public <T extends AttributeModel> T addAttributeModel(T attributeModel) {
        Objects.requireNonNull(attributeModel, "attributeModel must not be null");
        attributeModel.setPosition(getAttributeModels().size() + 1);
        attributeModels = lazyAdd(attributeModels, attributeModel);
        Collections.sort(attributeModels, (o1, o2) -> {
            return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
        });
        return attributeModel;
    }

    @Override
    public <T> T accept(AttributeModelVisitor<T> visitor) {
        return visitor.visitNested(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("occurs", getOccurs())
                .append("attributes", attributeModels)
                .toString();
    }

}
