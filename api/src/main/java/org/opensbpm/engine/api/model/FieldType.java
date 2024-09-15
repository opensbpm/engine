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
package org.opensbpm.engine.api.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
public enum FieldType {
    //TODO rename to AttributeType
    STRING(String.class),
    NUMBER(Integer.class),
    DECIMAL(BigDecimal.class),
    DATE(LocalDate.class),
    TIME(LocalTime.class),
    BOOLEAN(Boolean.class),
    BINARY(Binary.class);

    private final Class<?> type;

    FieldType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

}
