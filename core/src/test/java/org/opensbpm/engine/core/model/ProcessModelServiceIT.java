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

import org.junit.Test;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.examples.ExampleModels;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProcessModelServiceIT extends ServiceITCase {

    @Autowired
    private ProcessDefinitionPersistor definitionPersistor;

    @Autowired
    private ProcessModelService processModelService;

    @Test
    public void testDelete() {
        //given
        Long modelId = doInTransaction(() -> {
            ProcessDefinition processDefinition = new org.opensbpm.engine.xmlmodel.ProcessModel().unmarshal(ExampleModels.getDienstreiseantrag());
            ProcessModel processModel = definitionPersistor.saveDefinition(processDefinition);
            return processModelService.save(processModel).getId();
        });

        //when
        doInTransaction(() -> {
            ProcessModel processModel = entityManager.find(ProcessModel.class, modelId);
            processModelService.delete(processModel);
            return null;
        });

        //then
        ProcessModel result = entityManager.find(ProcessModel.class, modelId);
        assertThat(result, is(nullValue()));
    }

}
