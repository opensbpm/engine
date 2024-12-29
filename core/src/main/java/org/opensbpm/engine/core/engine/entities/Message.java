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
package org.opensbpm.engine.core.engine.entities;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.utils.entities.HasId;

@Entity(name = "message")
public class Message implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Subject sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private ObjectModel objectModel;

    private boolean consumed;

    protected Message() {
    }

    public Message(ObjectModel objectModel, Subject sender) {
        this.objectModel = Objects.requireNonNull(objectModel);
        this.sender = Objects.requireNonNull(sender);
    }

    @Override
    public Long getId() {
        return id;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public Subject getSender() {
        return sender;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

}
