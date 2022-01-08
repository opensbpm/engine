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
package org.opensbpm.engine.core.model;

import org.opensbpm.engine.core.junit.DataJpaTestCase;
import org.opensbpm.engine.core.model.RoleService.RoleRepository;
import org.opensbpm.engine.core.model.RoleService.RoleSpecifications;
import org.opensbpm.engine.core.model.entities.Role;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.assertTrue;

public class RoleSpecificationsIT extends DataJpaTestCase {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void withIds() {
        //given

        Long id1 = entityManager.persistAndGetId(new Role("Role 1"), Long.class);
        Long id2 = entityManager.persistAndGetId(new Role("Role 2"), Long.class);

        //when
        Collection<Role> result = roleRepository.findAll(RoleSpecifications.withIds(Arrays.asList(id1, id2)));

        //then
        assertThat(result, hasSize(2));
    }

    @Test
    public void withName() {
        //given

        Role role = new Role("role");
        role = entityManager.persistFlushFind(role);

        //when
        Optional<Role> result = roleRepository.findOne(RoleSpecifications.withName(role.getName()));

        //then
        assertTrue(result.isPresent());
        assertThat(result.get().getName(), is(role.getName()));
    }

}
