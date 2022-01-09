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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.opensbpm.engine.api.InstanceService;
import org.opensbpm.engine.api.ProcessNotFoundException;
import org.opensbpm.engine.api.instance.AuditTrail;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.core.engine.EngineConverter;
import org.opensbpm.engine.core.engine.ProcessInstanceService;
import org.opensbpm.engine.core.engine.SubjectTrailService;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.ExceptionFactory.newProcessNotFoundException;

@Service
public class InstanceServiceBoundary implements InstanceService {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private SubjectTrailService subjectTrailService;
    
    @Autowired
    private EngineConverter engineConverter;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public Collection<ProcessInfo> findAllByStates(Set<ProcessInstanceState> states) {
        return engineConverter.convertInstances(processInstanceService.findAllByStates(states));
    }

    @Transactional
    @Override
    public ProcessInfo stopProcess(ProcessRequest processRequest) throws ProcessNotFoundException {
        ProcessInstance processInstance = processInstanceService.cancelByUser(findInstance(processRequest));
        return engineConverter.convertInstance(processInstance);
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public List<AuditTrail> getAuditTrail(ProcessRequest processRequest) throws ProcessNotFoundException {
        ProcessInstance processInstance = findInstance(processRequest);
        return engineConverter.convertAuditTrails(subjectTrailService.getSubjectTrail(processInstance.getId()));
    }

    private ProcessInstance findInstance(ProcessRequest processRequest) throws ProcessNotFoundException {
        return processInstanceService.findById(processRequest.getId())
                .orElseThrow(newProcessNotFoundException(processRequest.getId()));
    }

}
