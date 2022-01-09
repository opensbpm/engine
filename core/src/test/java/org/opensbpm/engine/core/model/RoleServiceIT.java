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

import java.util.Optional;
import org.junit.Test;
import org.opensbpm.engine.api.ModelService;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.builder.ProcessBuilder;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.engine.UserService.UserRepository;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.RoleService.RoleRepository;
import org.opensbpm.engine.core.model.RoleService.RoleSpecifications;
import org.opensbpm.engine.core.model.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.opensbpm.engine.api.junit.CommonMatchers.value;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isRoleChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isRoleUserChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserProcessModelChangedEvent;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

/**
 * {@link RoleService} Integration-Test. Test the transactional-behaviour of the SeviceLayer against a real database.
 *
 */
public class RoleServiceIT extends ServiceITCase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelService modelService;

    @Autowired
    private RoleService roleService;

    @Test
    public void saveNewRoleWithGroupAndUser() {
        //given
        Role role = new Role("New Role");
        User userWithoutGroup = doInTransaction(() -> {
            return userRepository.save(new User("User without Group"));
        });
        role.addUser(userWithoutGroup);

        engineEventsCollector.clear();

        //when
        Role result = doInTransaction(() -> roleService.save(role));

        //then
        assertThat(result.getId(), is(notNullValue()));

        assertThat(engineEventsCollector, containsInAnyOrder(
                isRoleChangedEvent("New Role", Type.CREATE),
                isRoleUserChangedEvent(userWithoutGroup.getId(), Type.CREATE)
        ));
    }

    @Test
    public void saveUpdateWithProcessModelAndUsers() {
        //given
        ProcessModelInfo modelInfo = doInTransaction(() -> {
            ProcessDefinition processDefinition = new ProcessBuilder("process")
                    .addSubject(userSubject("starter", "starter-role").asStarter()
                            .addState(functionState("state-1").asStart()
                                    .toHead(functionState("end").asEnd()))
                    )
                    .addSubject(userSubject("subject", "subject-role")
                            .addState(functionState("state-1").asStart()
                                    .toHead(functionState("end").asEnd()))
                    )
                    .build();

            return modelService.save(processDefinition);
        });
        assertThat(modelInfo.getId(), is(notNullValue()));

        User user = doInTransaction(()
                -> userRepository.save(new User("username")));

        Role role = doInTransaction(() -> {
            Role one = roleRepository.findOne(RoleSpecifications.withName("starter-role"))
                    .orElseThrow(() -> new IllegalStateException());
            one.addUser(user);
            return one;
        });

        engineEventsCollector.clear();

        //when
        Role result = doInTransaction(() -> {
            return roleService.save(entityManager.merge(role));
        });

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(engineEventsCollector, containsInAnyOrder(
                isRoleChangedEvent("starter-role", Type.UPDATE),
                isRoleUserChangedEvent(user.getId(), Type.CREATE),
                isUserProcessModelChangedEvent(Type.CREATE)
        ));
    }

    @Test
    public void deleteWithUsers() {
        //given
        User user = doInTransaction(()
                -> userRepository.save(new User("username")));

        Role existingRole = doInTransaction(() -> {
            Role role = new Role("role");
            role.addUser(user);
            role = roleRepository.save(role);

            assertThat(role.getId(), is(notNullValue()));
            assertThat(role.getUsers(), hasItem(value(User::getId, is(user.getId()))));
            return role;
        });

        engineEventsCollector.clear();

        //when
        roleService.delete(existingRole);

        //then
        Optional<Role> optionalRole = roleRepository.findById(existingRole.getId());
        assertThat(optionalRole.isPresent(), is(false));

        assertThat(engineEventsCollector, containsInAnyOrder(
                isRoleChangedEvent("role", Type.DELETE),
                isRoleUserChangedEvent(user.getId(), Type.DELETE)
        ));
    }

}
