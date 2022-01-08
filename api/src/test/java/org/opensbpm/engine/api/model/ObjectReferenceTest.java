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

import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObjectReferenceTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given
        ObjectReference objectReference = ObjectReference.of("1","Some Text");

        //when
        ObjectReference result = DeserializerUtil.deserializeJaxb(ObjectReference.class, objectReference);

        //then
        assertThat(result.getId(), is("1"));
        assertThat(result.getDisplayName(), is("Some Text"));
    }

}
