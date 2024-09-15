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
package org.opensbpm.engine.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
public class ProcessModelInfo implements Serializable {

    private Long id;
    private String name;
    private String version;
    private String description;
    private ProcessModelState state;

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdAt;
    private List<SubjectModelInfo> subjectModels;

    public ProcessModelInfo() {
        //JAXB constructor
    }

    public ProcessModelInfo(
            Long id,
            String name,
            String version,
            String description,
            ProcessModelState state,
            LocalDateTime createdAt,
            List<SubjectModelInfo> subjectModels) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.state = state;
        this.createdAt = createdAt;
        this.subjectModels = Collections.unmodifiableList(subjectModels);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public ProcessModelState getState() {
        return state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<SubjectModelInfo> getSubjectModels() {
        return Collections.unmodifiableList(subjectModels);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("createdAt", createdAt)
                .toString();
    }

    @XmlRootElement
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class SubjectModelInfo implements Serializable {

        //TODO rename to id
        private Long smId;
        private String name;
        private Collection<RoleInfo> roles;

        public SubjectModelInfo() {
            //JAXB constructor
        }

        public SubjectModelInfo(Long smId, String name, Collection<RoleInfo> roles) {
            super();
            this.smId = smId;
            this.name = name;
            this.roles = Collections.unmodifiableCollection(roles);
        }

        public Long getSmId() {
            return smId;
        }

        public String getName() {
            return name;
        }

        public Collection<RoleInfo> getRoles() {
            return Collections.unmodifiableCollection(roles);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("id", smId)
                    .append("name", name)
                    .append("roles", roles)
                    .toString();
        }

        @XmlAccessorType(value = XmlAccessType.FIELD)
        public static class RoleInfo implements Serializable {

            public static RoleInfo of(String name) {
                RoleInfo roleInfo = new RoleInfo();
                roleInfo.name = Objects.requireNonNull(name, "name must be non null");
                return roleInfo;
            }

            private String name;

            public RoleInfo() {
                //JAXB constructor
            }

            public String getName() {
                return name;
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder()
                        .append(name)
                        .build();
            }

            @Override
            public boolean equals(Object obj) {
                return EqualsBuilder.reflectionEquals(this, obj);
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append("name", name)
                        .toString();
            }

        }
    }

}
