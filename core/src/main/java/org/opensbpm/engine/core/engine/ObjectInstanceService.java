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
import java.util.Objects;
import java.util.logging.Logger;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObjectInstanceService {

    private static final Logger LOGGER = Logger.getLogger(ObjectInstanceService.class.getName());

    @Autowired
    private ObjectInstanceRepository objectInstanceRepository;

    @Transactional
    public ObjectInstance retrieveObjectInstance(ProcessInstance instance, ObjectModel objectModel) {
        Objects.requireNonNull(objectModel, "objectModel must not be null");
        synchronized (instance) {
            return instance.getObjectInstance(objectModel)
                    .orElseGet(() -> {
                        return instance.addObjectInstance(objectModel);
                    });
        }
    }

    public List<ObjectInstance> saveAll(List<ObjectInstance> objectInstances) {
        return objectInstanceRepository.saveAll(objectInstances);
    }

    @Repository
    public interface ObjectInstanceRepository extends JpaSpecificationRepository<ObjectInstance, Long> {

    }

}
