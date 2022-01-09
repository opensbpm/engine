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
package org.opensbpm.engine.core.engine.entities;

import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityTestCase;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

public class SubjectTrailTest extends EntityTestCase<SubjectTrail> {

    public SubjectTrailTest() {
        super(SubjectTrail.class);
    }

    @Test
    public void testToString() {
        SubjectTrail instance = new SubjectTrail();

        String result = instance.toString();
        assertThat(result, not(emptyOrNullString()));
    }

}
