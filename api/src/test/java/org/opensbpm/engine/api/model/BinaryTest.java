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

import org.opensbpm.engine.api.model.Binary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import org.opensbpm.engine.api.DeserializerUtil;

public class BinaryTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given
        Binary binary = new Binary("application/octet-stream", new byte[]{1});

        //when
        Binary result = DeserializerUtil.deserializeJaxb(Binary.class, binary);

        //then
        assertThat(result.getMimeType(), is("application/octet-stream"));
        assertThat(result.getValue().length, is(1));
    }

}
