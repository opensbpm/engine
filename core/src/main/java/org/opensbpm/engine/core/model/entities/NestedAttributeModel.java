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

import javax.persistence.Entity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
//@Table(name = "tomanymodel", uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"name", "name"}),
//    @UniqueConstraint(columnNames = {"objectmodel", "position"})
//})
public class NestedAttributeModel extends AbstractContainerAttributeModel {

    protected NestedAttributeModel() {
        //JPA constructor
    }

    public NestedAttributeModel(ObjectModel objectModel, String name) {
        super(objectModel, name);
    }

    public NestedAttributeModel(AttributeModel parent, String name) {
        super(parent, name);
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
                .append("attributes", attributeModels)
                .toString();
    }

}
