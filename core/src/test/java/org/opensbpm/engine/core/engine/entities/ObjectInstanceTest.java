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
import static org.junit.Assert.assertNotNull;

public class ObjectInstanceTest extends EntityTestCase<ObjectInstance> {

    public ObjectInstanceTest() {
        super(ObjectInstance.class);
    }

    @Test
    public void testToString() {
        //given
        ObjectInstance instance = new ObjectInstance();

        //when
        String result = instance.toString();
        
        //then
        assertNotNull(result);
    }

}
