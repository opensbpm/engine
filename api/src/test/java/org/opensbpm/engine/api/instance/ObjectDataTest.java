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

import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.DeserializerUtil;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ObjectDataTest {

    @Test
    public void testDeserializeWithJaxb() throws Exception {
        //given
        ObjectData objectData = ObjectData.of("Data")
                .withDisplayName("displayName")
                .withData(new HashMap<>())
                .withId("id")
                .build();

        //when
        ObjectData result = DeserializerUtil.deserializeJaxb(ObjectDataRoot.class, 
                new ObjectDataRoot(objectData)).objectData;

        //then
        assertThat("wrong name", result.getName(), is("Data"));
        assertThat("wrong displayName", result.getDisplayName().get(), is("displayName"));
        assertThat("wrong data", result.getData(), is(notNullValue()));
        assertThat("wrong id", result.getId(), is("id"));
    }

    @Test
    public void testDeserializeWithJackson() throws Exception {
        //given
        ObjectData objectData = ObjectData.of("Data")
                .withDisplayName("displayName")
                .withData(new HashMap<>())
                .withId("id")
                .build();

        //when
        ObjectData result = DeserializerUtil.deserializeJackson(ObjectDataRoot.class, 
                new ObjectDataRoot(objectData)).objectData;

        //then
        assertThat("wrong name", result.getName(), is("Data"));
        assertThat("wrong displayName", result.getDisplayName().get(), is("displayName"));
        assertThat("wrong data", result.getData(), is(notNullValue()));
        assertThat("wrong id", result.getId(), is("id"));
    }

    @XmlRootElement
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class ObjectDataRoot{
        
        private ObjectData objectData;

        public ObjectDataRoot() {
        }

        public ObjectDataRoot(ObjectData objectData) {
            this.objectData = objectData;
        }

        
        public ObjectData getObjectData() {
            return objectData;
        }
        
    }
}
