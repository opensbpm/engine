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

import org.opensbpm.engine.api.InstanceService.ProcessRequest;
import org.opensbpm.engine.api.adapters.LocalDateTimeAdapter;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessInfo implements Serializable, ProcessRequest {

    private Long id;

    @EqualsExclude
    private ProcessModelInfo processModelInfo;

    @EqualsExclude
    private UserToken owner;

    @EqualsExclude
    private ProcessInstanceState state;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @EqualsExclude
    private LocalDateTime startTime;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    @EqualsExclude
    private LocalDateTime endTime;

    @EqualsExclude
    private List<SubjectStateInfo> subjects;

    public ProcessInfo() {
        //JAXB constructor
    }

    public ProcessInfo(final Long id,
            final ProcessModelInfo processModelInfo,
            final UserToken owner,
            final ProcessInstanceState state,
            final LocalDateTime startTime,
            final LocalDateTime endTime,
            final List<SubjectStateInfo> subjects) {
        this.id = id;
        this.processModelInfo = processModelInfo;
        this.owner = owner;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjects = Collections.unmodifiableList(subjects);
    }

    @Override
    public Long getId() {
        return id;
    }

    public ProcessModelInfo getProcessModelInfo() {
        return processModelInfo;
    }

    public UserToken getOwner() {
        return owner;
    }

    public ProcessInstanceState getState() {
        return state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<SubjectStateInfo> getSubjects() {
        return Collections.unmodifiableList(subjects);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("modelName", processModelInfo.getName())
                .append("state", state)
                .append("startTime", startTime)
                .append("subject", getSubjects())
                .toString();
    }

    @XmlRootElement
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class SubjectStateInfo implements Serializable {

        //TODO remove useless id
        private Long sId;
        private UserToken user;
        private String subjectName;
        private String stateName;
        private StateFunctionType stateFunctionType;
        @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
        private LocalDateTime lastChanged;

        public SubjectStateInfo() {
            super();
        }

        public SubjectStateInfo(final Long sId, final UserToken user, final String subjectName, final String stateName, final StateFunctionType stateFunctionType, final LocalDateTime lastChanged) {
            super();
            this.sId = sId;
            this.user = user;
            this.subjectName = subjectName;
            this.stateName = stateName;
            this.stateFunctionType = stateFunctionType;
            this.lastChanged = lastChanged;
        }

        public Long getSId() {
            return sId;
        }

        public UserToken getUser() {
            return user;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getStateName() {
            return stateName;
        }

        public StateFunctionType getStateFunctionType() {
            return stateFunctionType;
        }

        public LocalDateTime getLastChanged() {
            return lastChanged;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("id", sId)
                    .append("user", user)
                    .append("subjectName", subjectName)
                    .append("stateName", stateName)
                    .append("functionType", stateFunctionType)
                    .append("lastChanged", lastChanged)
                    .toString();
        }

        public enum StateFunctionType {
            SEND, RECEIVE, FUNCTION
        }
    }

}
