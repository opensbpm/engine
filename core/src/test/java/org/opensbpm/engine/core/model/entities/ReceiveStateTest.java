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

import org.opensbpm.engine.core.model.entities.MessageModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.junit.EntityTestCase;

import static org.opensbpm.engine.core.model.entities.StateVisitor.receiveState;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ReceiveStateTest extends EntityTestCase<ReceiveState> {

    public ReceiveStateTest() {
        super(ReceiveState.class);
    }

    @Test
    public void testGetMessageModels() {
        //given
        ReceiveState instance = new ReceiveState();

        //when
        Collection<MessageModel> result = instance.getMessageModels();
        
        //then
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testAccept() {
        //given
        ReceiveState instance = new ReceiveState();

        //when
        Object result = instance.accept(receiveState())
                .map(state -> new Object())
                .orElse(null);
        
        //then
        assertThat(result, is(notNullValue()));
    }

}
