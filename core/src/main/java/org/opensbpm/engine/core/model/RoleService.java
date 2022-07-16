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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.Role_;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.model.RoleService.RoleSpecifications.withIds;
import static org.opensbpm.engine.core.model.RoleService.RoleSpecifications.withName;
import static org.opensbpm.engine.utils.StreamUtils.mapToSet;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EngineEventPublisher eventPublisher;

    public Collection<Role> findAll() {
        return roleRepository.findAll();
    }

    public Collection<Role> findAllByIds(Collection<Long> roleIds) {
        return roleRepository.findAll(withIds(roleIds));
    }

    public Optional<Role> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findOne(withName(name));
    }

    public Role save(Role role) {
        Type type = role.getId() == null ? Type.CREATE : Type.UPDATE;
//        Set<Long> oldUsers = role.getId() == null ? Collections.emptySet()
//                : mapToSet(roleEntityService.retrieve(role.getId()).getAllUsers(), user -> user.getId());

        Role savedRole = roleRepository.save(role);
        eventPublisher.fireRoleChanged(role, type);

        //publish changes
        Set<Long> newUsers = savedRole.getAllUsers().stream()
                .map(user -> user.getId())
                .collect(Collectors.toSet());
//        publishUserEvents(savedRole, subtract(oldUsers, newUsers), Type.DELETE);
//        publishUserEvents(savedRole, subtract(newUsers, oldUsers), Type.CREATE);        
        eventPublisher.publishUserEvents(savedRole, savedRole.getAllUsers(), Type.CREATE);

        return savedRole;
    }

    @Transactional
    public void delete(Role role) {
        for (User user : new ArrayList<>(role.getUsers())) {
            user.removeRole(role);
            role.removeUser(user);
            eventPublisher.fireRoleUserChanged(role, user);
        }
        eventPublisher.fireRoleChanged(role, Type.DELETE);
        roleRepository.delete(role);
    }

    @Repository
    public interface RoleRepository extends JpaSpecificationRepository<Role, Long> {

    }

    static final class RoleSpecifications {

        public static Specification<Role> withIds(final Collection<Long> ids) {
            return (Root<Role> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> root.get(Role_.id).in(ids);
        }

        public static Specification<Role> withName(final String name) {
            return (Root<Role> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.equal(root.get(Role_.name), name);
        }

        private RoleSpecifications() {
        }

    }

}
