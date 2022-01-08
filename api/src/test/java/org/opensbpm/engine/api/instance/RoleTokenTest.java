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

import org.opensbpm.engine.api.instance.RoleToken;
import org.opensbpm.engine.api.DeserializerUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class RoleTokenTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given
        RoleToken role = RoleToken.of(Long.MIN_VALUE, "Role");

        //when
        RoleToken result = DeserializerUtil.deserializeJaxb(RoleToken.class, role);

        //then
        assertThat("no id", result.getId(), is(notNullValue()));
        assertThat("wrong name", result.getName(), is("Role"));
    }

    @Test
    public void deserializeWithJackson() throws Exception {
        //given
        RoleToken role = RoleToken.of(Long.MIN_VALUE, "Role");

        //when
        RoleToken result = DeserializerUtil.deserializeJackson(RoleToken.class, role);

        //then
        assertThat("no id", result.getId(), is(notNullValue()));
        assertThat("wrong name", result.getName(), is("Role"));
    }

}
