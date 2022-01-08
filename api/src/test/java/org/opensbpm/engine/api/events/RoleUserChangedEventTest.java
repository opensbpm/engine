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
package org.opensbpm.engine.api.events;

import org.opensbpm.engine.api.events.RoleUserChangedEvent;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.UserToken;
import org.junit.Test;

import static org.opensbpm.engine.api.DeserializerUtil.deserializeObject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class RoleUserChangedEventTest {

    @Test
    public void testSserialize() throws Exception {
        //given
        RoleUserChangedEvent engineEvent = new RoleUserChangedEvent(1L, new UserToken(), Type.CREATE);

        //when
        RoleUserChangedEvent result = deserializeObject(engineEvent);

        //then
        assertThat(result.getType(), is(Type.CREATE));
        assertThat(result.getSource(), is(notNullValue()));
        assertThat(result.getRoleId(), is(1L));
    }

}
