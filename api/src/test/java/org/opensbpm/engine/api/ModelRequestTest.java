/** *****************************************************************************
 * Copyright (C) 2022 Stefan Sedelmaier
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
 *****************************************************************************
 */
package org.opensbpm.engine.api;

import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.Test;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelRequestTest {

    @Test
    public void testOfWithLongInfoCreatesInstance() throws Exception {
        //given
        long id = Long.MIN_VALUE;

        //when
        ModelRequest modelRequest = ModelRequest.of(id);

        //then
        assertThat("ModelRequest not instantiated", modelRequest.getId(), is(id));
    }
    
    @Test
    public void testOfWithProcessModelInfoCreatesInstance() throws Exception {
        //arrange
        long id = 1l;
        ProcessModelInfo processModelInfo = new ProcessModelInfo(
                id,
                "name",
                "version",
                "description",
                ProcessModelState.ACTIVE,
                LocalDateTime.MAX,
                Collections.emptyList()
        );

        //act
        ModelRequest modelRequest = ModelRequest.of(processModelInfo);

        //assert
        assertThat("ModelRequest not instantiated", 
                modelRequest.getId(), is(id));
    }

}
