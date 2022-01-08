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
package org.opensbpm.engine.core.junit;

import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import javax.persistence.EntityManager;

public class RbacBuilder {

    public static RbacBuilder createUser(String username, String firstname, String lastname) {
        return new RbacBuilder(new User(username));
    }

    private final User user;

    private RbacBuilder(User user) {
        this.user = user;
    }

    public RbacBuilder withRole(Role role) {
        user.addRole(role);
        return this;
    }

    public User build(EntityManager em) {
        em.persist(user);
        em.flush();
        return user;
    }

}
