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
import java.util.Collections;
import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class SubjectStateInfoTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given
        UserToken userToken = UserToken.of(Long.MAX_VALUE, "user", Collections.emptySet());
        SubjectStateInfo subjectState = new SubjectStateInfo(Long.MIN_VALUE, userToken, "subjectName", "stateName", StateFunctionType.FUNCTION, LocalDateTime.MIN);

        //when
        SubjectStateInfo result = DeserializerUtil.deserializeJaxb(SubjectStateInfo.class, subjectState);

        //then
        assertNotNull(result);
        assertThat(result.toString(), result.getSId(), is(subjectState.getSId()));
        assertThat(result.toString(), result.getUser().getId(), is(subjectState.getUser().getId()));
        assertThat(result.toString(), result.getSubjectName(), is(subjectState.getSubjectName()));
        assertThat(result.toString(), result.getStateName(), is(subjectState.getStateName()));
        assertThat(result.toString(), result.getStateFunctionType(), is(subjectState.getStateFunctionType()));
        assertThat(result.toString(), result.getLastChanged(), is(subjectState.getLastChanged()));
    }

}
