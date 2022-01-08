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

import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import org.opensbpm.engine.core.engine.entities.ProcessInstance_;
import org.opensbpm.engine.core.engine.entities.Subject_;
import org.opensbpm.engine.core.engine.entities.UserSubject_;
import org.opensbpm.engine.core.model.entities.Role_;
import org.opensbpm.engine.core.model.entities.UserSubjectModel_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.opensbpm.engine.core.engine.UserSubjectService.UserSubjectSpecifications.ofUserOrRoles;
import static org.opensbpm.engine.core.engine.UserSubjectService.UserSubjectSpecifications.withSubjectId;

import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;

@Service
public class UserSubjectService {

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Optional<UserSubject> retrieveForWrite(Long subjectId) {
        return userSubjectRepository.findOne(withSubjectId(subjectId));
    }

    /**
     * search all assigned and unassigned {@link UserSubject}'s for the given {@link User}
     *
     * @param user
     * @return
     */
    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<UserSubject> findAllByUser(User user) {
        //PENDING filter locked subject
        return userSubjectRepository.findAll(ofUserOrRoles(user));
    }

    @Repository
    public interface UserSubjectRepository extends JpaSpecificationRepository<UserSubject, Long> {
    }

    static class UserSubjectSpecifications {

        static Specification<UserSubject> withSubjectId(Long subjectId) {
            return (Root<UserSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.equal(root.get(Subject_.id), subjectId);
        }

        static Specification<UserSubject> ofUserOrRoles(User user) {
            return (Root<UserSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                Join<UserSubject, ProcessInstance> processInstanceJoin = root.join(Subject_.processInstance);

                //query all UserSubjects with UserSubject.user == user
                Predicate assignedSubjects = cb.and(
                        cb.equal(root.get(UserSubject_.user), user)
                );

                //query all UserSubjects joining UserSubjectModel.roles with UserSubject.user == null
                Join<UserSubject, UserSubjectModel> subjectModelJoin = cb.treat(root.join(Subject_.subjectModel), UserSubjectModel.class);
                SetJoin<UserSubjectModel, Role> roleJoin = subjectModelJoin.join(UserSubjectModel_.roles);

                Predicate unassignedSubjects = cb.and(
                        root.get(UserSubject_.user).isNull(),
                        cb.isMember(user, roleJoin.get(Role_.users))
                );

                return cb.and(
                        cb.equal(processInstanceJoin.get(ProcessInstance_.state), ProcessInstanceState.ACTIVE),
                        cb.or(assignedSubjects, unassignedSubjects)
                );
            };

        }

        private UserSubjectSpecifications() {
        }

    }

}
