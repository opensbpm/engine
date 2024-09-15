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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "statesend")
public class SendState extends State {

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private SubjectModel receiver;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private ObjectModel objectModel;

    @Column(nullable = false)
    private boolean async;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(updatable = false)
    private State head;

    protected SendState() {
    }

    public SendState(String name, SubjectModel receiver, ObjectModel objectModel) {
        super(name);
        this.receiver = Objects.requireNonNull(receiver);
        this.objectModel = Objects.requireNonNull(objectModel);
    }

    public SubjectModel getReceiver() {
        return receiver;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setHead(State head) {
        this.head = head;
    }

    public State getHead() {
        return head;
    }

    @Override
    public <T> T accept(StateVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitSendState(this);
    }

    @Override
    public Collection<State> getHeads() {
        return head == null ? Collections.emptyList() : Arrays.asList(head);
    }

}
