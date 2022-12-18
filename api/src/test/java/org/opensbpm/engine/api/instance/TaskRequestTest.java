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

import java.time.LocalDateTime;
import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class TaskRequestTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //arrange
        TaskRequest taskRequest = new TaskRequest(Long.MIN_VALUE, new NextState(), LocalDateTime.MIN);
        
        //act
        TaskRequest result = DeserializerUtil.deserializeJaxb(TaskRequest.class, taskRequest);

        //assert
        assertThat(result.toString(), result.getId(), is(taskRequest.getId()));
        assertThat(result.toString(), result.getNextState(), is(taskRequest.getNextState()));
        assertThat(result.toString(), result.getLastChanged(), is(taskRequest.getLastChanged()));
        assertThat(result.toString(), result.getObjectData(), is(empty()));
    }
}
