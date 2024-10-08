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

import java.io.Serializable;
import java.util.Arrays;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Binary implements Serializable {

    private String mimeType;
    private byte[] value;

    public Binary() {
        //JAXB constructor
    }

    public Binary(String mimeType, byte[] value) {
        this.mimeType = mimeType;
        this.value = Arrays.copyOf(value, value.length);
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

}
