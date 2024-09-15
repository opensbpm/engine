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

import java.time.LocalDateTime;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.adapters.LocalDateTimeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuditTrail {
    //TODO merge with SubjectStateInfo

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime created;
    private String subjectName;
    private UserToken user;
    private String stateName;

    public AuditTrail() {
        //JAXB constructor
    }

    public AuditTrail(LocalDateTime created, String subjectName, UserToken user, String stateName) {
        this.created = created;
        this.subjectName = subjectName;
        this.user = user;
        this.stateName = stateName;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public UserToken getUser() {
        return user;
    }

    public String getStateName() {
        return stateName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("subjectName", subjectName)
                .append("user", user == null ? null : user.getName())
                .append("stateName", stateName)
                .append("created", created)
                .toString();
    }

}
