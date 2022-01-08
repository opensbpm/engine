/** *****************************************************************************
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
 *****************************************************************************
 */
package org.opensbpm.engine.api.instance;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.opensbpm.engine.api.EqualsMatchers.isEqualContract;

public class ProcessInfoTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given        
        UserToken userToken = UserToken.of(Long.MAX_VALUE, "user", Collections.emptySet());
        ProcessInfo processInfo = new ProcessInfo(Long.MIN_VALUE, new ProcessModelInfo(), userToken, ProcessInstanceState.ACTIVE, LocalDateTime.MIN, LocalDateTime.MAX, Arrays.asList(new SubjectStateInfo()));

        //when
        ProcessInfo result = DeserializerUtil.deserializeJaxb(ProcessInfo.class, processInfo);

        //then
        assertNotNull(result);

        assertThat(result.toString(), result.getId(), is(processInfo.getId()));
        assertThat(result.toString(), result.getProcessModelInfo().getName(), is(processInfo.getProcessModelInfo().getName()));
        assertThat(result.toString(), result.getProcessModelInfo().getVersion(), is(processInfo.getProcessModelInfo().getVersion()));
        assertThat(result.toString(), result.getOwner().getId(), is(processInfo.getOwner().getId()));
        assertThat(result.toString(), result.getState(), is(processInfo.getState()));
        assertThat(result.toString(), result.getStartTime(), is(processInfo.getStartTime()));
        assertThat(result.toString(), result.getEndTime(), is(processInfo.getEndTime()));
    }

    @Test
    public void reflectionHashCode() {
        //given
        ProcessInfo processInfo = new ProcessInfo();

        //when
        //then
        assertThat(processInfo.hashCode(), is(not(0)));
    }

    @Test
    public void testEquals() {
        //given
        ProcessInfo processInfo = new ProcessInfo();

        //when
        //then
        assertThat(processInfo, isEqualContract(new ProcessInfo()));
    }

}
