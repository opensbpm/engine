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
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserToken implements Serializable {
    //TODO rename to SubjectToken

    public static UserToken of(Long id, String name, Set<RoleToken> roles) {
        UserToken userToken = new UserToken();
        userToken.id = id;
        userToken.name = name;
        userToken.roles = new HashSet<>(roles);
        return userToken;
    }

    private Long id;

    @EqualsExclude
    private String name;

    private Set<RoleToken> roles;

    public UserToken() {
        //JAXB constructor
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<RoleToken> getRoles() {
        return emptyOrUnmodifiableSet(roles);
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
                .append("name", name)
                .append("roles", roles)
                .toString();
    }

}
