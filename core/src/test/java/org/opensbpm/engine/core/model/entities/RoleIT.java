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

import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

public class RoleIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        Role role = new Role();

        //when
        Role result = entityManager.persistFlushFind(role);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        Role role = new Role("role");
        entityManager.persistFlushFind(role);

        role = new Role("role");

        //when
        Role result = entityManager.persistFlushFind(role);

        //then
        fail("insert of the same role-name twice must throw exception, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        Role role = new Role("role");

        //when
        Role result = entityManager.persistFlushFind(role);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("role"));
    }

    @Test
    public void testAddUser() {
        //given
        User user = persistedUser("user");

        Role role = entityManager.persist(new Role("role"));
        assertThat(role.getId(), is(notNullValue()));

        //when
        role.addUser(user);
        Role result = entityManager.persistFlushFind(role);

        //then
        assertThat(result.getUsers(), hasSize(1));
    }

    @Test
    public void testRemoveUser() {
        //given
        User user = persistedUser("user");

        Role role = new Role("role");
        role.addUser(user);
        role = entityManager.persist(role);
        assertThat(role.getId(), is(notNullValue()));

        //when
        role.removeUser(user);
        Role result = entityManager.persistFlushFind(role);

        //then
        assertThat(result.getUsers(), hasSize(0));

        assertThat(find(User.class, user), is(notNullValue()));
    }

    @Test
    public void testDeleteWithUser() {
        //given
        User user = persistedUser("user");

        Role role = new Role("role");
        role.addUser(user);
        role = entityManager.persist(role);
        assertThat(role.getId(), is(notNullValue()));

        //when
        entityManager.remove(role);
        entityManager.flush();

        //then
        assertThat(find(Role.class, role), is(nullValue()));

        assertThat(find(User.class, user), is(notNullValue()));
    }

}
