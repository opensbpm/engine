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

import org.opensbpm.engine.api.ProcessNotFoundException;
import org.opensbpm.engine.api.instance.AuditTrail;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.core.engine.ValidationProviderManager;
import org.opensbpm.engine.core.engine.EngineConverter;
import org.opensbpm.engine.core.engine.ProcessInstanceService;
import org.opensbpm.engine.core.engine.StateChangeService;
import org.opensbpm.engine.core.engine.SubjectService;
import org.opensbpm.engine.core.engine.SubjectTrailService;
import org.opensbpm.engine.core.engine.UserSubjectService;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;

import static org.opensbpm.engine.core.junit.MockData.spyProcessInstance;
import static org.opensbpm.engine.core.junit.MockData.spyProcessModel;
import static org.opensbpm.engine.core.junit.MockData.spyUser;

import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.UserService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.script.ScriptEngine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.fail;

/**
 * Spring Mock Unit-Test for {@link InstanceServiceBoundary}
 */
@SpringBootTest(classes = {
    InstanceServiceBoundary.class,
    EngineConverter.class
})
@RunWith(SpringRunner.class)
public class ProcessServiceBoundaryTest {

    @Autowired
    private InstanceServiceBoundary processServiceBoundary;

    @MockBean
    private ProcessModelService processModelService;

    @MockBean
    private ProcessInstanceService processInstanceService;

    @MockBean
    private UserService userService;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private UserSubjectService userSubjectService;

    @MockBean
    private SubjectTrailService subjectTrailService;

    @MockBean
    private StateChangeService stateChangeService;

    @MockBean
    private EngineEventPublisher eventPublisher;

    @MockBean
    private ScriptEngine scriptEngine;

    @MockBean
    private ValidationProviderManager validationProviderManager;

    @Test(expected = ProcessNotFoundException.class)
    public void stopProcessWithWrongProcessInstanceId() throws Exception {
        //given
        Long piId = 1l;
        LocalDateTime lastChanged = LocalDateTime.now();
        when(processInstanceService.findById(piId)).thenReturn(Optional.empty());

        ProcessInfo info = new ProcessInfo(piId, null, null, ProcessInstanceState.ACTIVE, lastChanged, lastChanged, Collections.emptyList());

        //when
        ProcessInfo processInfo = processServiceBoundary.stopProcess(info);

        //then
        fail("stopProcess with wrong 'piId' must throw IllegalArgumentException but was " + processInfo);
    }

    @Test
    public void stopProcessSuccessFull() throws Exception {
        //given
        Long pmId = 1l;
        String processName = "ProcessModel Name";
        Long userId = 2l;
        Long piId = 3l;
        LocalDateTime lastChanged = LocalDateTime.now();

        ProcessModel processModel = spyProcessModel(pmId, processName);
        when(processModelService.findById(pmId)).thenReturn(Optional.of(processModel));

        User user = spyUser(userId, "username", "firstname", "lastname");
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        ProcessInstance processInstance = spyProcessInstance(piId, processModel, user);
        when(processInstanceService.findById(piId)).thenReturn(Optional.of(processInstance));

        when(processInstanceService.cancelByUser(processInstance)).thenReturn(processInstance);

        ProcessInfo info = new ProcessInfo(piId, null, null, ProcessInstanceState.ACTIVE, lastChanged, lastChanged, Collections.emptyList());

        //when
        ProcessInfo processInfo = processServiceBoundary.stopProcess(info);

        //then
        assertThat(processInfo, is(notNullValue()));
    }

    @Test(expected = ProcessNotFoundException.class)
    public void getAuditTrailWithWrongPiId() throws Exception {
        //given
        Long piId = 1l;
        LocalDateTime lastChanged = LocalDateTime.now();
        when(processInstanceService.findById(piId)).thenReturn(Optional.empty());

        ProcessInfo info = new ProcessInfo(piId, null, null, ProcessInstanceState.ACTIVE, lastChanged, lastChanged, Collections.emptyList());

        //when
        List<AuditTrail> auditTrails = processServiceBoundary.getAuditTrail(info);

        //then
        fail("getAuditTrail with wrong 'piId' must throw Exception but was " + auditTrails);
    }

    @Test
    public void getAuditTrailSuccessFull() throws Exception {
        //given
        Long pmId = 1l;
        String processName = "ProcessModel Name";
        Long userId = 1l;
        Long piId = 3l;
        LocalDateTime lastChanged = LocalDateTime.now();

        User user = spyUser(userId, "username", "firstname", "lastname");
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(pmId, processName);
        when(processModelService.findById(pmId)).thenReturn(Optional.of(processModel));

        ProcessInstance processInstance = spyProcessInstance(piId, processModel, user);
        when(processInstanceService.findById(piId)).thenReturn(Optional.of(processInstance));

        ProcessInfo info = new ProcessInfo(piId, null, null, ProcessInstanceState.ACTIVE, lastChanged, lastChanged, Collections.emptyList());

        //when
        List<AuditTrail> auditTrails = processServiceBoundary.getAuditTrail(info);

        //then
        assertThat(auditTrails, is(notNullValue()));
    }

}
