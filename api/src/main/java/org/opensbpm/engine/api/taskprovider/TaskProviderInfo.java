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
package org.opensbpm.engine.api.taskprovider;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskProviderInfo {

    private String name;

    public TaskProviderInfo() {
        //JAXB constructor
    }

    public TaskProviderInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProviderResource {

        private String name;

        private String mimeType;

        //@XmlAnyElement(lax = true)
        private Object resource;

        public ProviderResource() {
            //JAXB-Constructor
        }

        public ProviderResource(String name, String mimeType, Object resource) {
            this.name = name;
            this.mimeType = mimeType;
            this.resource = resource;
        }

        public String getName() {
            return name;
        }

        public String getMimeType() {
            return mimeType;
        }

        public Object getResource() {
            return resource;
        }

    }
}
