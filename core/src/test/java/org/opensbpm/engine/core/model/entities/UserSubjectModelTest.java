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
package org.opensbpm.engine.core.model.entities;

import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.junit.EntityTestCase;
import java.util.Collection;

import static org.junit.Assert.fail;

import org.junit.Test;

public class UserSubjectModelTest extends EntityTestCase<UserSubjectModel> {

    public UserSubjectModelTest() {
        super(UserSubjectModel.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetRolesUnmodifiable() {
        System.out.println("getSubjectModels unmodifiable");
        UserSubjectModel instance = new UserSubjectModel();

        Collection<Role> result = instance.getRoles();
        result.add(new Role("Role"));
        fail("List from getRoles must not be modifiable");
    }

}
