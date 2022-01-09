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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance_;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.engine.entities.UserSubject_;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.engine.ProcessInstanceService.ProcessInstanceSpecifications.withStates;
import static org.opensbpm.engine.core.engine.ProcessInstanceService.ProcessInstanceSpecifications.withUserAndState;

@Service
public class ProcessInstanceService {

    @Autowired
    private ProcessInstanceRepository instanceRepository;

    @Autowired
    private EngineEventPublisher eventPublisher;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public Collection<ProcessInstance> findAllByStates(Set<ProcessInstanceState> states) {
        return instanceRepository.findAll(withStates(states));
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<ProcessInstance> findAllByUserAndState(User user, final ProcessInstanceState state) {
        return instanceRepository.findAll(withUserAndState(user, state));
    }

    public Optional<ProcessInstance> findById(Long id) {
        return instanceRepository.findById(id);
    }

    @Transactional
    public ProcessInstance start(ProcessModel processModel, User startUser) {
        ProcessInstance savedInstance = save(new ProcessInstance(processModel, startUser));

        eventPublisher.fireProcessInstanceChanged(savedInstance, Type.CREATE);
        return savedInstance;
    }

    @Transactional
    public ProcessInstance cancelByUser(ProcessInstance processInstance) {
        return stop(processInstance, ProcessInstanceState.CANCELLED_BY_USER);
    }

    @Transactional
    public ProcessInstance cancelBySystem(ProcessInstance processInstance, String message) {
        processInstance.setCancelMessage(message);
        return stop(processInstance, ProcessInstanceState.CANCELLED_BY_SYSTEM);
    }

    @Transactional
    public ProcessInstance finish(ProcessInstance processInstance) {
        return stop(processInstance, ProcessInstanceState.FINISHED);
    }

    private ProcessInstance stop(ProcessInstance processInstance, ProcessInstanceState state) {
        if (!processInstance.isActive()) {
            throw new IllegalStateException("Process " + processInstance.toString() + " not active");
        }
        processInstance.setState(state);
        ProcessInstance savedInstance = save(processInstance);
        eventPublisher.fireProcessInstanceChanged(savedInstance, Type.UPDATE);
        return savedInstance;
    }

    private ProcessInstance save(ProcessInstance processInstance) {
        return instanceRepository.save(processInstance);
    }

    @Repository
    public interface ProcessInstanceRepository extends JpaSpecificationRepository<ProcessInstance, Long> {
    }

    static class ProcessInstanceSpecifications {

        public static Specification<ProcessInstance> withStates(Collection<ProcessInstanceState> states) {
            return (Root<ProcessInstance> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> root.get(ProcessInstance_.state).in(states);
        }

        public static Specification<ProcessInstance> withState(ProcessInstanceState state) {
            return (Root<ProcessInstance> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.equal(root.get(ProcessInstance_.state), state);
        }

        public static Specification<ProcessInstance> withUser(User user) {
            return (Root<ProcessInstance> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                final ListJoin<ProcessInstance, UserSubject> subjectJoin = cb.treat(root.join(ProcessInstance_.subjects), UserSubject.class);
                return cb.equal(subjectJoin.get(UserSubject_.user), user);
            };
        }

        public static Specification<ProcessInstance> withUserAndState(User user, ProcessInstanceState state) {
            return Specification.where(withState(state)).and(withUser(user));
        }

        private ProcessInstanceSpecifications() {
        }
    }

}
