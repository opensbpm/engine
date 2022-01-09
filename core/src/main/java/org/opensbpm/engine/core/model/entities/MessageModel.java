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
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.opensbpm.engine.core.utils.entities.HasId;

@Entity
@Table(name = "messagemodel", uniqueConstraints = @UniqueConstraint(
        columnNames = {"statereceive", "objectmodel"})
)
public class MessageModel implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "objectmodel", nullable = false, updatable = false)
    private ObjectModel objectModel;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private State head;

    protected MessageModel() {
    }

    public MessageModel(ObjectModel objectModel, State head) {
        this.objectModel = Objects.requireNonNull(objectModel);
        this.head = Objects.requireNonNull(head);
    }

    @Override
    public Long getId() {
        return id;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public State getHead() {
        return head;
    }

}
