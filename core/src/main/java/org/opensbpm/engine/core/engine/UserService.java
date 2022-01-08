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

import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.opensbpm.engine.core.engine.entities.User_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.opensbpm.engine.core.engine.UserService.UserSpecifications.withIds;
import static org.opensbpm.engine.core.engine.UserService.UserSpecifications.withUsername;

import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EngineEventPublisher eventPublisher;

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public Collection<User> findAllByIds(Collection<Long> ids) {
        return userRepository.findAll(withIds(ids));
    }

    public Optional<User> findByName(String name) {
        return userRepository.findOne(withUsername(name));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        Type type = user.getId() == null ? Type.CREATE : Type.UPDATE;
        User saveUser = userRepository.save(user);
        eventPublisher.fireUserChanged(user, type);
        return saveUser;
    }

    public void delete(User user) {
        for (Role role : new ArrayList<>(user.getRoles())) {
            role.removeUser(user);
        }
        userRepository.delete(user);
        eventPublisher.fireUserChanged(user, Type.DELETE);
    }

    @Repository
    public interface UserRepository extends JpaSpecificationRepository<User, Long> {

    }

    static class UserSpecifications {

        public static Specification<User> withIds(final Collection<Long> ids) {
            return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> root.get(User_.id).in(ids);
        }

        public static Specification<User> withUsername(final String username) {
            return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.equal(root.get(User_.username), username);
        }

        public static Specification<User> withRole(Role role) {
            return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.isMember(role, root.get(User_.roles));
        }

        private UserSpecifications() {
        }

    }
}
