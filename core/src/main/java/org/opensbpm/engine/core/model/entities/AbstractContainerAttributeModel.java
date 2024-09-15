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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity
public abstract class AbstractContainerAttributeModel extends AttributeModel implements HasId, IsAttributeParent, Serializable {
    
    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy(value = "position ASC")
    protected List<AttributeModel> attributeModels;

    protected AbstractContainerAttributeModel() {
        //JPA constructor
    }

    protected AbstractContainerAttributeModel(ObjectModel objectModel, String name) {
        super(objectModel, name);
    }

    protected AbstractContainerAttributeModel(AttributeModel parent, String name) {
        super(parent, name);
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
    
}
