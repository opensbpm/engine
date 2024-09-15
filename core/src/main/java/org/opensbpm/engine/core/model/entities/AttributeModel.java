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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.opensbpm.engine.core.utils.entities.HasId;

@Entity
@Table(name = "attributemodel", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"objectmodel", "name"}),
    @UniqueConstraint(columnNames = {"objectmodel", "position"})
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AttributeModel implements HasId, Serializable {
    //TODO rename to 
    // * SimpleAttributeModel
    //      * optional StringAttributeModel and so on
    // * NestedAttributeModel
    // * IndexedAttributeModel

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "objectmodel", updatable = false)
    private ObjectModel objectModel;

    @ManyToOne
    @JoinColumn(name = "parent", updatable = false)
    private AttributeModel parent;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Column
    @Min(0)
    private int position;

    protected AttributeModel() {
        //JPA constructor
    }

    protected AttributeModel(ObjectModel objectModel, String name) {
        this.objectModel = Objects.requireNonNull(objectModel);
        this.name = Objects.requireNonNull(name);
    }

    protected AttributeModel(AttributeModel parent, String name) {
        this.parent = Objects.requireNonNull(parent);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public Long getId() {
        return id;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public AttributeModel getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public abstract <T> T accept(AttributeModelVisitor<T> visitor);

}
