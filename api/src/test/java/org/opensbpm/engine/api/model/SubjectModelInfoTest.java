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
package org.opensbpm.engine.api.model;

import org.junit.Test;
import org.opensbpm.engine.api.DeserializerUtil;
import org.opensbpm.engine.api.model.ProcessModelInfo.SubjectModelInfo;
import org.opensbpm.engine.api.model.ProcessModelInfo.SubjectModelInfo.RoleInfo;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;

public class SubjectModelInfoTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        //given
        SubjectModelInfo subjectModelInfo = new SubjectModelInfo(Long.MIN_VALUE, "name", asList(
                RoleInfo.of("Role 1"),
                RoleInfo.of("Role 2")
        ));

        //when
        SubjectModelInfo result = DeserializerUtil.deserializeJaxb(SubjectModelInfo.class, subjectModelInfo);

        //then
        assertNotNull(result);
        assertThat(result.toString(), result.getSmId(), is(subjectModelInfo.getSmId()));
        assertThat(result.toString(), result.getName(), is(subjectModelInfo.getName()));
        assertThat(result.toString(), result.getRoles(), hasSize(2));
    }

}
