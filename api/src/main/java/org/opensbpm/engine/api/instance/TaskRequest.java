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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.adapters.LocalDateTimeAdapter;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskRequest implements Serializable {

    private Long id;
    private NextState nextState;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime lastChanged;

    private List<ObjectData> objectDatas;

    public TaskRequest() {
        //JAXB constructor
    }

    public TaskRequest(Long taskId, NextState nextState, LocalDateTime lastChanged) {
        this.id = Objects.requireNonNull(taskId, "taskid must not be null");
        this.nextState = Objects.requireNonNull(nextState, "nextState must not be null");
        this.lastChanged = Objects.requireNonNull(lastChanged, "lastChanged must not be null");
    }

    public Long getId() {
        return id;
    }

    public NextState getNextState() {
        return nextState;
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    public List<ObjectData> getObjectData() {
        return emptyOrUnmodifiableList(objectDatas);
    }

    public void setObjectDatas(List<ObjectData> objectDatas) {
        this.objectDatas = Collections.unmodifiableList(objectDatas);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("nextState", nextState)
                .append("lastChanged", lastChanged)
                .append("objectDatas", objectDatas)
                .toString();
    }
}
