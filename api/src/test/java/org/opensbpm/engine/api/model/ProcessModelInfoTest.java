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
package org.opensbpm.engine.api.model;

import org.opensbpm.engine.api.DeserializerUtil;
import org.opensbpm.engine.api.model.ProcessModelInfo.SubjectModelInfo;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class ProcessModelInfoTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        System.out.println("JAXB-Marshalling");

        List<SubjectModelInfo> subjectModels = Arrays.asList(new SubjectModelInfo(Long.MIN_VALUE, "name", Arrays.asList("Role")));
        ProcessModelInfo processModelInfo = new ProcessModelInfo(Long.MIN_VALUE, "name", "1.1", "description", ProcessModelState.ACTIVE, LocalDateTime.MAX, subjectModels);
        ProcessModelInfo result = DeserializerUtil.deserializeJaxb(ProcessModelInfo.class, processModelInfo);
        assertNotNull(result);

        assertThat(result.toString(), result.getId(), is(processModelInfo.getId()));
        assertThat(result.toString(), result.getName(), is(processModelInfo.getName()));
        assertThat(result.toString(), result.getDescription(), is(processModelInfo.getDescription()));
        assertThat(result.toString(), result.getCreatedAt(), is(processModelInfo.getCreatedAt()));
        assertThat(result.toString(), result.getSubjectModels(), is(not(empty())));
        assertThat(result.toString(), result.getState(), is(processModelInfo.getState()));
        assertThat(result.toString(), result.getVersion(), is(processModelInfo.getVersion()));
    }

}
