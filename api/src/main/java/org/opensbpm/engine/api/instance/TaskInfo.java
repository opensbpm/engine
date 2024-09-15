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
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.adapters.LocalDateTimeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskInfo implements Serializable {

    /**
     * task.id, subject.id *
     */
    private Long id;

    private Long processId;

    private String processName;

    private String stateName;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime lastChanged;

    public TaskInfo() {
        //JAXB constructor
    }

    public TaskInfo(Long taskId, Long processId, String processName, String stateName, LocalDateTime lastChanged) {
        this.id = Objects.requireNonNull(taskId, "taskId must not be null");
        this.processId = Objects.requireNonNull(processId, "processId must not be null");

        this.processName = processName;
        this.stateName = stateName;
        this.lastChanged = lastChanged;
    }

    /**
     * Id of Task
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * id of process, see {@link ProcessInfo#getId()}
     *
     * @return id of the process
     */
    public Long getProcessId() {
        return processId;
    }

    public String getProcessName() {
        return processName;
    }

    public String getStateName() {
        return stateName;
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("processId", processId)
                .append("processName", processName)
                .append("stateName", stateName)
                .append("lastChanged", lastChanged)
                .toString();
    }

}
