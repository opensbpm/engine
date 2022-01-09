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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ModelVersion_;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ProcessModel_;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.Role_;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.State_;
import org.opensbpm.engine.core.model.entities.SubjectModel_;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel_;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.model.ProcessModelService.ProcessModelSpecifications.newestVersion;
import static org.opensbpm.engine.core.model.ProcessModelService.ProcessModelSpecifications.withStartableFor;
import static org.opensbpm.engine.core.model.ProcessModelService.ProcessModelSpecifications.withStates;

@Service
public class ProcessModelService {

    @Autowired
    private ProcessModelRepository modelRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EngineEventPublisher eventPublisher;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<ProcessModel> findAllByStates(Set<ProcessModelState> states) {
        return modelRepository.findAll(withStates(states));
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Collection<ProcessModel> findAllStartableByUser(User user) {
        return modelRepository.findAll(withStartableFor(user));
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Collection<ProcessModel> findAllStartableByRole(final Role role) {
        return modelRepository.findAll(withStartableFor(role));
    }

    public Optional<ProcessModel> findById(Long id) {
        return modelRepository.findById(id);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Optional<ProcessModel> findNewestVersion(String name, int major) {
        return modelRepository.findOne(newestVersion(name, major));
    }

    @Transactional
    public ProcessModel save(ProcessModel processModel) {
        //FIXME
//        if (processModel.getId() != null) {
//            throw new IllegalStateException("can't update ProcessModel");
//        }
        ProcessModel savedModel = modelRepository.save(processModel);
        eventPublisher.fireProcessModelChanged(savedModel, Type.CREATE);
        return savedModel;
    }

    @Transactional
    public void updateState(ProcessModel processModel, ProcessModelState newState) {
        processModel.setState(newState);
        modelRepository.save(processModel);

        eventPublisher.fireProcessModelUpdate(processModel);
    }

    @Transactional
    public void delete(ProcessModel processModel) {
        eventPublisher.fireProcessModelChanged(processModel, Type.DELETE);

        //FIXME don't disable referential integrity; don't delete ProcessModel if there are starter processes
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        modelRepository.delete(processModel);
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();

    }

    @Repository
    public interface ProcessModelRepository extends JpaSpecificationRepository<ProcessModel, Long> {
    }

    static class ProcessModelSpecifications {

        public static Specification<ProcessModel> newestVersion(String name, int major) {
            return (Root<ProcessModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                Join<ProcessModel, ModelVersion> versionJoin = root.join(ProcessModel_.version);

                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<ProcessModel> subRoot = subquery.from(ProcessModel.class);
                Join<ProcessModel, ModelVersion> subVersionJoin = subRoot.join(ProcessModel_.version);

                subquery.select(cb.max(subVersionJoin.get(ModelVersion_.minor)));
                subquery.where(cb.and(
                        cb.equal(subRoot.get(ProcessModel_.name), name),
                        cb.equal(subVersionJoin.get(ModelVersion_.major), major)
                ));

                return cb.and(
                        cb.equal(root.get(ProcessModel_.name), name),
                        cb.equal(versionJoin.get(ModelVersion_.major), major),
                        cb.in(versionJoin.get(ModelVersion_.minor)).value(subquery)
                );
            };
        }

        public static Specification<ProcessModel> withStates(Collection<ProcessModelState> states) {
            return (Root<ProcessModel> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> root.get(ProcessModel_.state).in(states);
        }

        public static Specification<ProcessModel> withStartableFor(Role role) {
            return (Root<ProcessModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                query.distinct(true);
                /*
                WHERE pm.state = 'ACTIVE'
                AND state.event_type = 'START'
                AND sender.subject_model_type IS NULL
                AND role-group-user
                 */

                Join<ProcessModel, UserSubjectModel> subjectModelJoin = cb.treat(root.join(ProcessModel_.subjectModels), UserSubjectModel.class);
                ListJoin<UserSubjectModel, State> stateJoin = subjectModelJoin.join(SubjectModel_.states);
                return cb.and(
                        cb.equal(root.get(ProcessModel_.state), ProcessModelState.ACTIVE),
                        cb.equal(subjectModelJoin.get(SubjectModel_.starter), true),
                        cb.equal(stateJoin.get(State_.eventType), StateEventType.START),
                        subjectModelJoin.join(UserSubjectModel_.roles).in(role)
                );
            };
        }

        public static Specification<ProcessModel> withStartableFor(User user) {
            return (Root<ProcessModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                /*
                TODO a ProcessModel is startable if a functionState is in root-states of stategraph;
                SubjectModel.starter and eventType.START is useless
                 */

                query.distinct(true);
                /*
                WHERE pm.state = 'ACTIVE'
                AND state.event_type = 'START'
                AND sender.subject_model_type IS NULL
                AND role-group-user
                 */
                Join<ProcessModel, UserSubjectModel> subjectModelJoin = cb.treat(root.join(ProcessModel_.subjectModels), UserSubjectModel.class);
                ListJoin<UserSubjectModel, State> stateJoin = subjectModelJoin.join(SubjectModel_.states);
                SetJoin<UserSubjectModel, Role> rolesJoin = subjectModelJoin.join(UserSubjectModel_.roles);
                return cb.and(
                        cb.equal(root.get(ProcessModel_.state), ProcessModelState.ACTIVE),
                        cb.equal(subjectModelJoin.get(SubjectModel_.starter), true),
                        cb.equal(stateJoin.get(State_.eventType), StateEventType.START),
                        rolesJoin.join(Role_.users).in(user)
                );
            };
        }

        private ProcessModelSpecifications() {
        }
    }

}
