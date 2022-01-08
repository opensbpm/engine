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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.adapters.MapAdapter;

@XmlAccessorType(value = XmlAccessType.FIELD)
public class ObjectData implements Serializable {

    /**
     * create a new {@link ObjectDataBuilder} with name.
     * @return a new {@link ObjectDataBuilder} instance 
     */
    public static ObjectDataBuilder of(String name) {
        return new ObjectDataBuilder(name);
    }

    public static final class ObjectDataBuilder {

        private final String name;
        private String displayName;
        private Map<Long, Serializable> data;
        private String id;

        private ObjectDataBuilder(String name) {
            this.name = Objects.requireNonNull(name, "name must be non null");
        }

        /**
         * setter for {@link ObjectData#displayName}
         * @param displayName {@link ObjectData#displayName}
         * @return the same instance as the caller
         */
        public ObjectDataBuilder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * setter for {@link ObjectData#data}
         * @param data {@link ObjectData#data}
         * @return the same instance as the caller
         */
        public ObjectDataBuilder withData(Map<Long, Serializable> data) {
            this.data = data;
            return this;
        }

        /**
         * setter for {@link ObjectData#id}
         * @param id {@link ObjectData#id}
         * @return the same instance as the caller
         */
        public ObjectDataBuilder withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * build an {@link ObjectData}-instance
         * @return the same instance as the caller
         */
        public ObjectData build() {
            ObjectData objectData = new ObjectData();
            objectData.id = id;
            objectData.name = Objects.requireNonNull(name, "name must be non null");
            objectData.displayName = displayName;
            objectData.data = new HashMap<>(data);
            return objectData;
        }
    }

    private String id;

    @XmlElement(required = true)
    private String name;

    private String displayName;

    @XmlJavaTypeAdapter(value = MapAdapter.class)
    private Map<Long, Serializable> data;

    protected ObjectData() {
        //JAXB Constructor
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public Map<Long, Serializable> getData() {
        return data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("displayName", displayName)
                .append("attributeData", data)
                .toString();
    }

}
