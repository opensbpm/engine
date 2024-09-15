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
package org.opensbpm.engine.api.adapters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.adapters.MapAdapter.ValueElement;

public class MapAdapter extends XmlAdapter<ValueElement[], Map<Long, Serializable>> {

    @Override
    public ValueElement[] marshal(Map<Long, Serializable> value) throws Exception {
        ValueElement[] mapElements = new ValueElement[value.size()];
        int i = 0;
        for (Map.Entry<Long, Serializable> entry : value.entrySet()) {
            mapElements[i++] = new ValueElement(entry.getKey(), entry.getValue());
        }

        return mapElements;
    }

    @Override
    public Map<Long, Serializable> unmarshal(ValueElement[] value) throws Exception {
        Map<Long, Serializable> map = new HashMap<>();
        for (ValueElement mapelement : value) {
            map.put(mapelement.id, mapelement.value);
        }
        return map;
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class ValueElement implements Serializable {

        @XmlElement
        private Long id;

        @XmlJavaTypeAdapter(SerializableAdapter.class)
        private Serializable value;

        public ValueElement() {
            //JAXB constructor
        }

        public ValueElement(Long id, Serializable value) {
            this.id = Objects.requireNonNull(id);
            this.value = value;
        }

        public Long getName() {
            return id;
        }

        public Serializable getValue() {
            return value;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("name", id)
                    .append("value", value)
                    .toString();
        }

    }
}
