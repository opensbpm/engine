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
package org.opensbpm.engine.core.engine;

import org.junit.Test;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserChangedEvent;

/**
 * {@link UserService} Integration-Test. Test the transactional-behaviour of the SeviceLayer against a real database.
 *
 */
public class UserServiceIT extends ServiceITCase {

    @Autowired
    private UserService userService;

    @Test
    public void testSave() {
        //given
        User user = new User("username");

        //when
        User result = userService.save(user);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("username"));

        assertThat(engineEventsCollector, contains(
                isUserChangedEvent(result.getId(), Type.CREATE)
        ));

    }

    @Test
    public void testDelete() {
        //given
        User user = doInTransaction(() -> {
            User newUser = new User("username");
            entityManager.persist(newUser);
            entityManager.flush();
            return newUser;
        });
        Long userId = user.getId();

        //when
        userService.delete(user);

        //then
        user = entityManager.find(User.class, user.getId());
        assertThat(user, is(nullValue()));

        assertThat(engineEventsCollector, contains(
                isUserChangedEvent(userId, Type.DELETE)
        ));

    }

}
