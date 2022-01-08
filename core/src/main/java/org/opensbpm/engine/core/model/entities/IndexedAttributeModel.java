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

import org.opensbpm.engine.api.model.definition.Occurs;
import javax.persistence.Entity;

@Entity
public class IndexedAttributeModel extends NestedAttributeModel {

    public IndexedAttributeModel() {
    }

    public IndexedAttributeModel(ObjectModel objectModel, String name) {
        super(objectModel, name, Occurs.UNBOUND);
    }

    public IndexedAttributeModel(AttributeModel parent, String name) {
        super(parent, name, Occurs.UNBOUND);
    }

    @Override
    public <T> T accept(AttributeModelVisitor<T> visitor) {
        return visitor.visitIndexed(this);
    }
}
