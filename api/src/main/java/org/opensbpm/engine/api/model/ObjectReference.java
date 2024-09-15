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
 * ****************************************************************************
 */
package org.opensbpm.engine.api.model;

import java.io.Serializable;
import java.util.HashMap;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.HashCodeExclude;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectReference implements Serializable {

    public static ObjectReference of(String id, String displayName) {
        ObjectReference objectReference = new ObjectReference();
        objectReference.id = id;
        objectReference.displayName = displayName;
        return objectReference;
    }

    private String id;
    
    @HashCodeExclude
    @EqualsExclude
    private String displayName;

    protected  ObjectReference() {
        //JAXB Constructor
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public HashMap<String,String> toMap(){
        HashMap hashMap = new HashMap();
        hashMap.put("id", id);
        hashMap.put("displayName", displayName);
        return hashMap;
    }
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    
}
