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

import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.opensbpm.engine.api.adapters.LocalDateTimeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskResponse implements Serializable {

    public static TaskResponse of(Long id, List<NextState> nextStates, LocalDateTime lastChanged, List<ObjectSchema> objectSchemas, List<ObjectData> datas) {
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.id = Objects.requireNonNull(id, "id must be non null");
        taskResponse.nextStates = Collections.unmodifiableList(nextStates);
        taskResponse.lastChanged = Objects.requireNonNull(lastChanged);
        taskResponse.schemas = Collections.unmodifiableList(objectSchemas);
        taskResponse.datas = Collections.unmodifiableList(datas);
        return taskResponse;
    }

    private Long id;

    private List<NextState> nextStates;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime lastChanged;

    private List<ObjectSchema> schemas;

    private List<ObjectData> datas;

    public TaskResponse() {
        //JAXB constructor
    }

    public Long getId() {
        return id;
    }

    public List<NextState> getNextStates() {
        return emptyOrUnmodifiableList(nextStates);
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    public List<ObjectSchema> getSchemas() {
        return emptyOrUnmodifiableList(schemas);
    }

    public List<ObjectData> getDatas() {
        return emptyOrUnmodifiableList(datas);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("schemas", schemas)
                .append("datas", datas)
                .append("lastChanged", lastChanged)
                .toString();
    }

}
