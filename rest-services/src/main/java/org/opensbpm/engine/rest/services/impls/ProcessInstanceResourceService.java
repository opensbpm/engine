/*******************************************************************************
 * Copyright (C) 2024 Stefan Sedelmaier
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
package org.opensbpm.engine.rest.services.impls;

import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.SearchFilter;
import org.opensbpm.engine.api.ProcessNotFoundException;
import org.opensbpm.engine.api.InstanceService.ProcessRequest;
import org.opensbpm.engine.api.instance.ProcessInfo;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Objects;
import java.util.logging.Logger;
import jakarta.ws.rs.NotFoundException;
import org.opensbpm.engine.rest.api.ProcessInstanceResource;
import org.opensbpm.engine.rest.api.dto.instance.Audits;
import org.opensbpm.engine.rest.api.dto.instance.Processes;
import org.springframework.stereotype.Component;
import org.opensbpm.engine.api.InstanceService;


@Component
public class ProcessInstanceResourceService implements ProcessInstanceResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessInstanceResourceService.class.getName());

    private final InstanceService instanceService;

    public ProcessInstanceResourceService(InstanceService instanceService) {
        this.instanceService = Objects.requireNonNull(instanceService, "InstanceService must not be null");
    }

    @Override
    public Processes search(SearchFilter searchFilter) {
//        SearchSpecificationBuilder<ProcessInstance> searchSpecificationBuilder = new SearchSpecificationBuilder<ProcessInstance>(searchFilter) {
//            @Override
//            protected Specification<ProcessInstance> createSpecification(Criteria criteria) {
//                if ("state".equals(criteria.getKey())) {
//                    return ProcessInstanceSpecifications.withStates(
//                            Collections.singleton(ProcessInstanceState.valueOf(criteria.getValue().toString()))
//                    );
//                }
//                return super.createSpecification(criteria);
//            }
//        };
//        return processInstanceController.findAll(searchSpecificationBuilder.build()).stream()
//                .map(processInstance -> createProcessInfo(processInstance))
//                .collect(Collectors.toList());
        return new Processes(new ArrayList<>(instanceService.findAllByStates(EnumSet.allOf(ProcessInstanceState.class))));
    }

    @Override
    public ProcessInfo retrieve(Long id) {
        //TODO use and implement SearchFilter
        try {
            return instanceService.findById(ProcessRequest.of(id));
        } catch (ProcessNotFoundException ex) {
            throw new NotFoundException(ex.getMessage(), ex);
        }
    }

    @Override
    public Audits retrieveAudit(Long id) {
        try {
            return new Audits(instanceService.getAuditTrail(ProcessRequest.of(id)));
        } catch (ProcessNotFoundException ex) {
            throw new NotFoundException(ex.getMessage(), ex);
        }
    }

    @Override
    public ProcessInfo stop(Long id) {
        try {
            return instanceService.stopProcess(ProcessRequest.of(id));
        } catch (ProcessNotFoundException ex) {
            throw new NotFoundException(ex.getMessage(), ex);
        }
    }

}
