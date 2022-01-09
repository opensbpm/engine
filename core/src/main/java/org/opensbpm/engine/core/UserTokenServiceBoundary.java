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
package org.opensbpm.engine.core;

import java.util.List;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.UserNotFoundException;
import org.opensbpm.engine.api.UserTokenService;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.core.engine.UserService;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.model.RoleService;
import org.opensbpm.engine.core.model.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.ExceptionFactory.newUserNotFoundException;
import static org.opensbpm.engine.core.engine.EngineConverter.convertUser;

@Service
public class UserTokenServiceBoundary implements UserTokenService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Transactional
    @Override
    public UserToken registerUser(TokenRequest tokenRequest) {
        String username = tokenRequest.getUsername();
        User user = userService.findByName(username)
                .orElseGet(() -> createUser(tokenRequest));
        return convertUser(user);
    }

    @Transactional
    @Override
    public UserToken retrieveToken(TokenRequest tokenRequest) throws UserNotFoundException {
        return convertUser(getUser(tokenRequest));
    }

    private User getUser(TokenRequest tokenRequest) throws UserNotFoundException {
        return userService.findByName(tokenRequest.getUsername())
                .orElseThrow(newUserNotFoundException(tokenRequest.getUsername()));
    }

    private User createUser(TokenRequest tokenRequest) {
        User user = new User(tokenRequest.getUsername());
        saveRoles(tokenRequest)
                .forEach(role -> user.addRole(role));
        return userService.save(user);
    }

    private List<Role> saveRoles(TokenRequest tokenRequest) {
        return tokenRequest.getRoles().stream()
                .map(roleName -> {
                    Role role = roleService.findByName(roleName)
                            .orElseGet(() -> new Role(roleName));
                    return roleService.save(role);
                })
                .collect(Collectors.toList());
    }

}
