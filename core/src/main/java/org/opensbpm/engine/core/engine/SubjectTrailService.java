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

import java.util.List;
import jakarta.persistence.criteria.Join;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance_;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.SubjectTrail;
import org.opensbpm.engine.core.engine.entities.SubjectTrail_;
import org.opensbpm.engine.core.engine.entities.Subject_;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.engine.SubjectTrailService.SubjectTrailSpecifications.withProcessInstance;

@Service
public class SubjectTrailService {

    @Autowired
    private SubjectTrailRepository subjectTrailRepository;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    public List<SubjectTrail> getSubjectTrail(Long processId) {
        return subjectTrailRepository.findAll(withProcessInstance(processId), Sort.by(
                new Order(Direction.ASC, "lastModified"),
                new Order(Direction.ASC, "id")
        ));
    }

    @Repository
    public interface SubjectTrailRepository extends JpaSpecificationRepository<SubjectTrail, Long> {
    }

    static class SubjectTrailSpecifications {

        static Specification<SubjectTrail> withProcessInstance(Long id) {
            return (root, query, cb) -> {
                Join<SubjectTrail, Subject> subjectJoin = root.join(SubjectTrail_.subject);
                Join<Subject, ProcessInstance> processJoin = subjectJoin.join(Subject_.processInstance);
                return cb.equal(processJoin.get(ProcessInstance_.id), id);
            };
        }

        private SubjectTrailSpecifications() {
        }

    }
}
