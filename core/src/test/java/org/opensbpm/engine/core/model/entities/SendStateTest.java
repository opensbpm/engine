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
package org.opensbpm.engine.core.model.entities;

import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityTestCase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opensbpm.engine.core.model.entities.StateVisitor.sendState;

public class SendStateTest extends EntityTestCase<SendState> {

    public SendStateTest() {
        super(SendState.class);
    }

    @Test
    public void testSetHead() throws ReflectiveOperationException {
        testSetManyToOne("head", State.class, FunctionState.class);
    }

    @Test
    public void testAccept() {
        //given
        SendState instance = new SendState();

        //when
        Object result = instance.accept(sendState())
                .map(state -> new Object())
                .orElse(null);
        
        //then
        assertThat(result, is(notNullValue()));
    }

}
