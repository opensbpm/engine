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
package org.opensbpm.engine.core.engine.entities;

import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.entities.Role;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class UserIT extends EntityDataTestCase {

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        User users = new User();

        //when
        User result = entityManager.persistFlushFind(users);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testUniqueConstraint() {
        //given
        User user = entityManager.persistFlushFind(new User("user"));
        assertThat(user.getId(), is(notNullValue()));

        User newUser = new User("user");

        //when
        PersistenceException persistenceException = assertThrows("UniqueKey doesn't work", PersistenceException.class, () -> {
            User result = entityManager.persistFlushFind(newUser);
            fail("insert of the same role-name twice must throw exception, but was " + result);
        });

        //then
        assertThat("UniqueKey doesn't work", persistenceException.getCause(),
                is(instanceOf(ConstraintViolationException.class)));
    }

    @Test
    public void testInsert() {
        //given
        User user = new User("username");

        //when
        User result = entityManager.persistFlushFind(user);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is(user.getName()));
    }

    @Test
    public void testAddRole() {
        //given
        Role role = persistedRole("role");

        User user = entityManager.persist(new User("user"));
        assertThat(user.getId(), is(notNullValue()));

        //when
        user.addRole(role);
        User result = entityManager.persistFlushFind(user);

        //then
        assertThat(result.getRoles(), hasSize(1));
    }

    @Test
    public void testRemoveRole() {
        //given
        Role role = persistedRole("role");

        User user = new User("user");
        user.addRole(role);
        user = entityManager.persist(user);
        assertThat(user.getId(), is(notNullValue()));

        //when
        user.removeRole(role);
        User result = entityManager.persistFlushFind(user);

        //then
        assertThat(result.getRoles(), hasSize(0));

        assertThat(find(Role.class, role), is(notNullValue()));
    }

    @Test
    public void testDeleteWithRole() {
        //given
        Role role = persistedRole("role");

        User user = new User("user");
        user.addRole(role);
        user = entityManager.persist(user);
        assertThat(user.getId(), is(notNullValue()));

        //when
        role.removeUser(user);
        entityManager.remove(user);
        entityManager.flush();

        //then
        assertThat(find(User.class, user), is(nullValue()));

        assertThat(find(Role.class, role), is(notNullValue()));
    }

}
