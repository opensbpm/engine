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
package org.opensbpm.engine.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class DeserializerUtil {

    @SuppressWarnings("unchecked")
    public static <T> T deserializeObject(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
        }
        byte[] bytes = bos.toByteArray();
        assertThat(bytes.length, is(not(0)));

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }

    public static <T> T deserializeJaxb(Class<T> typeClass, T object) throws JAXBException {
        JAXBContext jaxbc = JAXBContext.newInstance(typeClass);

        StringWriter stringWriter = new StringWriter();
        final Marshaller marshaller = jaxbc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(object, stringWriter);
        assertFalse(stringWriter.toString().isEmpty());

//        System.out.println("" + stringWriter.toString());
        StringReader stringReader = new StringReader(stringWriter.toString());
        T result = typeClass.cast(jaxbc.createUnmarshaller().unmarshal(stringReader));
        assertNotNull(result);
        return result;
    }

    public static <T> T deserializeJackson(Class<T> typeClass, T object) throws JsonProcessingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());

        final String json = mapper.writeValueAsString(object);
        assertFalse(json.isEmpty());

        final T result = mapper.readValue(json, typeClass);
        assertNotNull(result);
        return result;
    }

    private DeserializerUtil() {
    }

}
