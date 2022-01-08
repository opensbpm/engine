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

import org.opensbpm.engine.utils.StreamUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public class UserTokenRequest {

    public static UserTokenRequest of(String name, Collection<String> roles) {
        UserTokenRequest request = new UserTokenRequest();
        request.name = name;
        request.roles = new HashSet<>(roles);
        return request;
    }

    private String name;
    private Set<String> roles;

    public UserTokenRequest() {
        //JAXB constructor
    }

    public String getUsername() {
        return name;
    }

    public Set<String> getRoles() {
        return StreamUtils.emptyOrUnmodifiableSet(roles);
    }

}
