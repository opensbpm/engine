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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class SerializableAdapter extends XmlAdapter<byte[], Serializable> {

    @Override
    public Serializable unmarshal(byte[] v) throws Exception {
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(v))) {
            return (Serializable) in.readObject();
        }
    }

    @Override
    public byte[] marshal(Serializable v) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutput out = new ObjectOutputStream(baos)) {
            out.writeObject(v);
            out.flush();
            return baos.toByteArray();
        }
    }

}
