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
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.utils.entities.HasDisplayName;
import org.opensbpm.engine.core.utils.entities.HasId;

@Entity
@Table(name = "state", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subjectmodel", "name"})
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class State implements HasId, HasDisplayName, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false)
    private String name;

    private String displayName;

    @Column
    @Enumerated(EnumType.STRING)
    private StateEventType eventType;

    protected State() {
    }

    protected State(String name) {
        this.name = Objects.requireNonNull(name, "name must be non null");
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public StateEventType getEventType() {
        return eventType;
    }

    public void setEventType(StateEventType eventType) {
        this.eventType = eventType;
    }

    public boolean isStart() {
        return StateEventType.START == getEventType();
    }

    public boolean isEnd() {
        return StateEventType.END == getEventType();
    }

    public abstract <T> T accept(StateVisitor<T> visitor);

    public abstract Collection<State> getHeads();

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", name)
                .append("displayName", displayName)
                .append("eventType", eventType)
                .toString();
    }

}
