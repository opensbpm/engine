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

public class AuditTrailTest {

    @Test
    public void testDeserializeWithJaxb() throws Exception {
        //given
        AuditTrail auditTrail = new AuditTrail(LocalDateTime.MAX, "subject", new UserToken(), "stateName");

        //when
        AuditTrail result = DeserializerUtil.deserializeJaxb(AuditTrail.class, auditTrail);

        //then
        assertThat(result.getCreated(), is(auditTrail.getCreated()));
        assertThat(result.getSubjectName(), is(auditTrail.getSubjectName()));
        assertThat(result.getUser(), is(auditTrail.getUser()));
        assertThat(result.getStateName(), is(auditTrail.getStateName()));
    }

}
