/** *****************************************************************************
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
 * ****************************************************************************
 */
package org.opensbpm.engine.core.engine;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import static org.opensbpm.engine.core.engine.ObjectInstanceService.ObjectInstanceSpecifications.withObjectModel;
import static org.opensbpm.engine.core.engine.ObjectInstanceService.ObjectInstanceSpecifications.withProcessInstance;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.engine.entities.ObjectInstance_;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class ObjectInstanceService {

    private static final Logger LOGGER = Logger.getLogger(ObjectInstanceService.class.getName());

    @Autowired
    private ObjectInstanceRepository objectInstanceRepository;

    public ObjectInstance retrieveObjectInstance(ProcessInstance process, ObjectModel objectModel) {
        Objects.requireNonNull(objectModel, "ObjectModel must not be null");
            return objectInstanceRepository.findByObjectModelForWrite(process, objectModel)
                    .orElseGet(() -> process.addObjectInstance(objectModel));
    }

    public List<ObjectInstance> saveAll(List<ObjectInstance> objectInstances) {
        return objectInstanceRepository.saveAll(objectInstances);
    }

    @Repository
    public interface ObjectInstanceRepository extends JpaSpecificationRepository<ObjectInstance, Long> {

        default Optional<ObjectInstance> findByObjectModelForWrite(ProcessInstance instance, ObjectModel objectModel) {
            return findOne(
                    withProcessInstance(instance)
                            .and(withObjectModel(objectModel))
            );
        }

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        public Optional<ObjectInstance> findOne(Specification<ObjectInstance> spec);

    }

    static class ObjectInstanceSpecifications {

        public static Specification<ObjectInstance> withProcessInstance(ProcessInstance processInstance) {
            return (Root<ObjectInstance> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.equal(root.get(ObjectInstance_.processInstance), processInstance);
        }

        public static Specification<ObjectInstance> withObjectModel(ObjectModel objectModel) {
            return (Root<ObjectInstance> root, CriteriaQuery<?> query, CriteriaBuilder cb)
                    -> cb.equal(root.get(ObjectInstance_.objectModel), objectModel);
        }
    }
}
