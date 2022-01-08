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

import org.opensbpm.engine.core.ModelServiceBoundary;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.builder.ProcessBuilder;

import static org.opensbpm.engine.api.model.builder.DefinitionFactory.serviceSubject;

import org.opensbpm.engine.api.model.definition.ProcessDefinition;

import static org.opensbpm.engine.core.junit.MockData.spyProcessModel;
import static org.opensbpm.engine.core.junit.MockData.spyUserSubjectModel;
import static org.opensbpm.engine.core.junit.MockData.spyWithId;

import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.ProcessDefinitionPersistor;
import org.opensbpm.engine.core.model.ModelConverter;
import org.opensbpm.engine.core.model.RoleService;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Spring Mock Unit-Test for {@link ModelServiceBoundary}
 */
@SpringBootTest(
        classes = {ModelServiceBoundary.class, ProcessDefinitionPersistor.class})
@RunWith(SpringRunner.class)
public class ModelServiceBoundaryTest {

    @Autowired
    private ModelServiceBoundary modelServiceBoundary;

    @MockBean
    private ProcessModelService processModelService;

    @Autowired
    private ProcessDefinitionPersistor definitionPersistor;

    @MockBean
    private RoleService roleService;

    @MockBean
    private EngineEventPublisher eventPublisher;

    @Test
    @SuppressWarnings("unchecked")
    public void testFindAllByStates() {
        //given
        final ProcessModel processModel = spyProcessModel(1l, "Process Model");
        when(processModelService.findAllByStates(any(Set.class))).thenReturn(Arrays.asList(processModel));

        //when
        Collection<ProcessModelInfo> result = modelServiceBoundary.findAllByStates(EnumSet.allOf(ProcessModelState.class));

        //then
        assertThat(result, is(not(empty())));
    }

    @Test
    public void testRetrieveDefinition() throws Exception {
        //given
        long modelId = 1l;

        final ProcessModel processModel = spyProcessModel(1l, "Process Model");
        UserSubjectModel starter = spyUserSubjectModel(0l, processModel, "User", new Role("Role"));
        processModel.setStarterSubject(starter);

        when(processModelService.findById(modelId)).thenReturn(Optional.of(processModel));

        ProcessModelInfo modelInfo = ModelConverter.convertModel(processModel);

        //when
        ProcessDefinition result = modelServiceBoundary.retrieveDefinition(modelInfo);

        //then
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void testSave() {
        //given
        ProcessDefinition processDefinition = new ProcessBuilder("Process")
                .addSubject(serviceSubject("Starter").asStarter())
                .build();

        when(processModelService.findNewestVersion(anyString(), anyInt())).thenReturn(Optional.empty());

        long pmId = 1l;
        when(processModelService.save(any(ProcessModel.class)))
                .thenAnswer((InvocationOnMock invocation)
                        -> spyWithId(pmId, (ProcessModel) invocation.getArgument(0)));

        //when
        ProcessModelInfo result = modelServiceBoundary.save(processDefinition);

        //then
        assertThat(result.getName(), is("Process"));
        assertThat(result.getVersion(), is("0.0"));

    }

    @Test
    public void testSaveWithIncrementMinor() {
        //given
        ProcessDefinition processDefinition = new ProcessBuilder("Process")
                .addSubject(serviceSubject("Starter").asStarter())
                .build();
        ProcessModel existingModel = new ProcessModel("Process", new ModelVersion(0, 0));
        when(processModelService.findNewestVersion(anyString(), anyInt()))
                .thenReturn(Optional.of(existingModel));

        long pmId = 1l;
        when(processModelService.save(any(ProcessModel.class)))
                .thenAnswer((InvocationOnMock invocation)
                        -> spyWithId(pmId, (ProcessModel) invocation.getArgument(0)));

        //when
        ProcessModelInfo result = modelServiceBoundary.save(processDefinition);

        //then
        assertThat(result.getName(), is("Process"));
        assertThat(result.getVersion(), is("0.1"));

    }

    @Test
    public void testUpdateState() throws Exception {
        //given
        final ProcessModelState state = ProcessModelState.ACTIVE;
        long pmId = 1l;
        final ProcessModel processModel = spyProcessModel(1l, "Process Model");
        when(processModelService.findById(pmId)).thenReturn(Optional.of(processModel));

        ProcessModelInfo modelInfo = ModelConverter.convertModel(processModel);

        //when
        modelServiceBoundary.updateState(modelInfo, state);

        //then
        verify(processModelService, times(1)).updateState(processModel, state);

    }

    @Test
    public void testDelete() throws Exception {
        //given
        long pmId = 1l;
        final ProcessModel processModel = spyProcessModel(1l, "Process Model");
        when(processModelService.findById(pmId)).thenReturn(Optional.of(processModel));

        ProcessModelInfo modelInfo = ModelConverter.convertModel(processModel);

        //when
        modelServiceBoundary.delete(modelInfo);

        //then
        verify(processModelService, times(1)).delete(processModel);
    }

}
