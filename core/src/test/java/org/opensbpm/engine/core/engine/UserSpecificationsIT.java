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

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.opensbpm.engine.core.engine.UserService.UserRepository;
import org.opensbpm.engine.core.engine.UserService.UserSpecifications;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.DataJpaTestCase;
import org.opensbpm.engine.core.model.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

public class UserSpecificationsIT extends DataJpaTestCase {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void withIds() {
        //given

        Long id1 = entityManager.persistAndGetId(new User("user 1"), Long.class);
        Long id2 = entityManager.persistAndGetId(new User("user 2"), Long.class);

        //when
        Collection<User> result = userRepository.findAll(UserSpecifications.withIds(Arrays.asList(id1, id2)));

        //then
        assertThat(result, hasSize(2));
    }

    @Test
    public void withUsername() {
        //given
        User user = new User("username");
        user = entityManager.persistFlushFind(user);

        //when
        Optional<User> result = userRepository.findOne(UserSpecifications.withUsername(user.getUsername()));

        //then
        assertTrue(result.isPresent());
        assertThat(result.get().getUsername(), is(user.getUsername()));
    }

    @Test
    public void withRole() {
        //given
        Role role = entityManager.persist(new Role("name"));
        User user = new User("username");
        user.addRole(role);
        user = entityManager.persistFlushFind(user);

        //when
        User result = userRepository.findOne(UserSpecifications.withRole(role))
                .orElseThrow(() -> new IllegalStateException());

        //then
        assertThat(result.getUsername(), is(user.getUsername()));
    }

}
