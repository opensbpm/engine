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
 *****************************************************************************
 */
package org.opensbpm.engine.core;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opensbpm.engine.api.EngineException;
import org.opensbpm.engine.api.ModelNotFoundException;
import org.opensbpm.engine.api.ModelService;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.model.ProcessDefinitionPersistor;
import org.opensbpm.engine.core.model.ProcessModelConverter;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.opensbpm.engine.core.ExceptionFactory.newModelNotFoundException;
import static org.opensbpm.engine.core.model.ModelConverter.convertModel;
import static org.opensbpm.engine.core.model.ModelConverter.convertModels;
import org.springframework.transaction.annotation.Isolation;

@Service
public class ModelServiceBoundary implements ModelService {

    @Autowired
    private ProcessModelService processModelService;

    @Autowired
    private ProcessDefinitionPersistor definitionPersistor;

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public List<ProcessModelInfo> findAllByStates(Set<ProcessModelState> states) {
        return convertModels(processModelService.findAllByStates(states));
    }

    @Transactional(readOnly = true, noRollbackFor = Exception.class)
    @Override
    public ProcessDefinition retrieveDefinition(ModelRequest modelRequest) throws ModelNotFoundException {
        return ProcessModelConverter.convert(findModel(modelRequest));
    }

    @Transactional
    @Override
    public ProcessModelInfo save(ProcessDefinition definition) {
        Objects.requireNonNull(definition, "processDefinition must not be null");
        ProcessModel processModel = definitionPersistor.saveDefinition(definition);
        return convertModel(processModel);
    }

    @Transactional(rollbackFor = EngineException.class)
    @Override
    public void updateState(ModelRequest modelRequest, ProcessModelState newState) throws ModelNotFoundException {
        ProcessModel processModel = findModel(modelRequest);
        processModelService.updateState(processModel, newState);
    }

    @Transactional(rollbackFor = EngineException.class)
    @Override
    public void delete(ModelRequest modelRequest) throws ModelNotFoundException {
        ProcessModel processModel = findModel(modelRequest);
        processModelService.delete(processModel);
    }

    private ProcessModel findModel(ModelRequest modelRequest) throws ModelNotFoundException {
        return processModelService.findById(modelRequest.getId())
                .orElseThrow(newModelNotFoundException(modelRequest.getId()));
    }

}
