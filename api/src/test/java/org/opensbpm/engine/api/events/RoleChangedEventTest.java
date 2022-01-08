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

import org.junit.Test;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.RoleToken;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opensbpm.engine.api.DeserializerUtil.deserializeObject;

public class RoleChangedEventTest {

    @Test
    public void testSserialize() throws Exception {
        //given
        RoleChangedEvent engineEvent = new RoleChangedEvent(new RoleToken(), Type.CREATE);

        //when
        RoleChangedEvent result = deserializeObject(engineEvent);

        //then
        assertThat(result.getType(), is(Type.CREATE));
        assertThat(result.getSource(), is(notNullValue()));
    }

}
