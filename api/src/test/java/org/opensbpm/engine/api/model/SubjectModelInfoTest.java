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
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class SubjectModelInfoTest {

    @Test
    public void deserializeWithJaxb() throws Exception {
        System.out.println("JAXB-Marshalling");

        SubjectModelInfo subjectModelDTO = new SubjectModelInfo(Long.MIN_VALUE, "name", Arrays.asList("Role 1", "Role 2"));
        SubjectModelInfo result = DeserializerUtil.deserializeJaxb(SubjectModelInfo.class, subjectModelDTO);
        assertNotNull(result);

        assertThat(result.toString(), result.getSmId(), is(subjectModelDTO.getSmId()));
        assertThat(result.toString(), result.getName(), is(subjectModelDTO.getName()));
        assertThat(result.toString(), result.getRoles(), contains("Role 1", "Role 2"));
    }

}
