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

import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import javax.persistence.PersistenceException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;

import org.hibernate.exception.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UserIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        User user = new User("user");
        entityManager.persistFlushFind(user);

        user = new User("user");

        //when
        User result = entityManager.persistFlushFind(user);

        //then
        fail("insert of the same role-name twice must throw exception, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        User user = new User("username");

        //when
        User result = entityManager.persistFlushFind(user);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getUsername(), is(user.getUsername()));
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
