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
package org.opensbpm.engine.core;

import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.EngineServiceBoundary;

import static org.opensbpm.engine.core.junit.MockData.spyFunctionState;
import static org.opensbpm.engine.core.junit.MockData.spyObjectModel;
import static org.opensbpm.engine.core.junit.MockData.spyProcessInstance;
import static org.opensbpm.engine.core.junit.MockData.spyProcessModel;
import static org.opensbpm.engine.core.junit.MockData.spySimpleAttributeModel;
import static org.opensbpm.engine.core.junit.MockData.spyUser;
import static org.opensbpm.engine.core.junit.MockData.spyUserSubject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.script.ScriptEngine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.opensbpm.engine.api.EngineService.ObjectRequest;
import org.opensbpm.engine.api.ModelNotFoundException;
import org.opensbpm.engine.api.UserNotFoundException;
import org.opensbpm.engine.api.instance.AutocompleteResponse;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.SourceMap;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.engine.ValidationProviderManager;
import org.opensbpm.engine.core.engine.ValidationService;
import org.opensbpm.engine.core.engine.EngineConverter;
import org.opensbpm.engine.core.engine.ProcessInstanceService;
import org.opensbpm.engine.core.engine.StateChangeService;
import org.opensbpm.engine.core.engine.SubjectService;
import org.opensbpm.engine.core.engine.SubjectTrailService;
import org.opensbpm.engine.core.engine.UserService;
import org.opensbpm.engine.core.engine.UserSubjectService;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.ModelConverter;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.api.spi.AutocompleteProvider;

import static org.opensbpm.engine.core.junit.MockData.spySendState;

/**
 * Spring Mock Unit-Test for {@link EngineServiceBoundary}
 */
@SpringBootTest(classes = {
    EngineServiceBoundary.class,
    EngineConverter.class,
    ValidationService.class
})
@RunWith(SpringRunner.class)
public class EngineServiceBoundaryTest {

    @Autowired
    private EngineServiceBoundary engineServiceBoundary;

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

    @Test
    public void findStartableProcessModels() throws Exception {
        //given
        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(1l, "Process Model");
        when(processModelService.findAllStartableByUser(user)).thenReturn(Arrays.asList(processModel));

        //when
        Collection<ProcessModelInfo> result = engineServiceBoundary.findStartableProcessModels(userToken);

        //then
        assertThat(result, is(not(empty())));
    }

    @Test(expected = UserNotFoundException.class)
    public void startProcessWithWrongUserId() throws Exception {
        //given
        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        //when
        TaskInfo taskInfo = engineServiceBoundary.startProcess(userToken, null);

        //then
        fail("startProcess with wrong 'user' must throw IllegalArgumentException but was " + taskInfo);
    }

    @Test(expected = ModelNotFoundException.class)
    public void startProcessWithWrongProcessModelId() throws Exception {
        //given
        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModelInfo modelInfo = new ProcessModelInfo();

        //when
        TaskInfo taskInfo = engineServiceBoundary.startProcess(userToken, modelInfo);

        //then
        fail("startProcess with wrong 'pmId' must throw IllegalArgumentException but was " + taskInfo);
    }

    @Test(expected = IllegalStateException.class)
    public void startProcessWithoutVisibleCurrentState() throws Exception {
        //given
        Long modelId = 2l;
        String processName = "ProcessModel Name";
        Long processId = 3l;
        Long subjectId = 4l;
        LocalDateTime lastChanged = LocalDateTime.now();

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        when(processModelService.findById(modelId)).thenReturn(Optional.of(processModel));

        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));
        processModel.setStarterSubject(userSubjectModel);

        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, user);
        FunctionState functionState = spyFunctionState(5l, userSubjectModel, "Function");
        functionState.setEventType(StateEventType.END);
        userSubject.setCurrentState(functionState);
        when(userSubject.getLastChanged()).thenReturn(lastChanged);

        when(processInstanceService.start(any(ProcessModel.class), any(User.class)))
                .thenReturn(processInstance);

        when(subjectService.createSubject(processInstance, userSubjectModel, user))
                .thenReturn(userSubject);

        ProcessModelInfo modelInfo = ModelConverter.convertModel(processModel);

        //when
        TaskInfo taskInfo = engineServiceBoundary.startProcess(userToken, modelInfo);

        //then
        fail("startProcess with initial end-state must throw Exception but was " + taskInfo);
    }

    @Test
    public void startProcessSuccessFull() throws Exception {
        //given
        Long modelId = 2l;
        String processName = "ProcessModel Name";
        Long processId = 3l;
        Long subjectId = 4l;
        LocalDateTime lastChanged = LocalDateTime.now();

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        when(processModelService.findById(modelId)).thenReturn(Optional.of(processModel));

        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));
        processModel.setStarterSubject(userSubjectModel);

        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, user);
        userSubject.setCurrentState(spyFunctionState(5l, userSubjectModel, "Function"));
        when(userSubject.getLastChanged()).thenReturn(lastChanged);

        when(processInstanceService.start(any(ProcessModel.class), any(User.class)))
                .thenReturn(processInstance);

        when(subjectService.createSubject(processInstance, userSubjectModel, user))
                .thenReturn(userSubject);

        ProcessModelInfo modelInfo = ModelConverter.convertModel(processModel);

        //when
        TaskInfo taskInfo = engineServiceBoundary.startProcess(userToken, modelInfo);

        //then
        assertThat(taskInfo, is(notNullValue()));
        assertThat(taskInfo.getProcessId(), is(processId));
        assertThat(taskInfo.getId(), is(subjectId));
        assertThat(taskInfo.getProcessName(), is(processName));
        assertThat(taskInfo.getLastChanged(), is(lastChanged));
    }

    /* missing tests for 
    findAllByStates
    findAllByUserAndState
    findAllByUser
     */
    @Test(expected = TaskOutOfDateException.class)
    public void getTaskResponseWithOutOfDateTaskInfo() throws Exception {
        //given
        String processName = "Process Model";
        String stateName = "Test Function";

        Long modelId = 1l;
        Long processId = 3l;
        Long subjectId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        //create subject without user!
        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, null);
        userSubject.setCurrentState(spyFunctionState(5l, userSubjectModel, stateName));
        when(subjectService.findById(subjectId)).thenReturn(Optional.of(userSubject));

        //when
        TaskInfo taskInfo = new TaskInfo(subjectId, processId, processName, stateName, LocalDateTime.MIN);
        TaskResponse result = engineServiceBoundary.getTaskResponse(userToken, taskInfo);

        //then
        fail("getTaskResponse with out-dated taskInfo must throw TaskOutOfDateException but was " + result);
    }

    @Test(expected = TaskOutOfDateException.class)
    public void getTaskResponseWithAlreadySwitchedState() throws Exception {
        //given
        String processName = "Process Model";
        String stateName = "Test Function";

        Long modelId = 1l;
        Long userId = 2l;
        Long processId = 3l;
        Long subjectId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        //create subject without user!
        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, null);
        FunctionState functionState = spyFunctionState(5l, userSubjectModel, stateName);
        functionState.setEventType(StateEventType.END);
        userSubject.setCurrentState(functionState);
        when(subjectService.findById(subjectId)).thenReturn(Optional.of(userSubject));

        //when
        TaskInfo taskInfo = new TaskInfo(subjectId, processId, processName, stateName, LocalDateTime.MAX);
        TaskResponse result = engineServiceBoundary.getTaskResponse(userToken, taskInfo);

        //then
        fail("getTaskResponse with already executed taskInfo must throw TaskOutOfDateException but was " + result);
    }

    @Test
    public void getTaskResponseSuccessful() throws Exception {
        //given
        String processName = "Process Model";
        String stateName = "Test Function";

        Long modelId = 1l;
        Long processId = 3l;
        Long subjectId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        //create subject without user!
        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, null);
        FunctionState functionState = spyFunctionState(5l, userSubjectModel, stateName);
        userSubject.setCurrentState(functionState);
        when(subjectService.findById(subjectId)).thenReturn(Optional.of(userSubject));

        //when
        TaskInfo taskInfo = new TaskInfo(subjectId, processId, processName, stateName, LocalDateTime.MAX);
        TaskResponse taskResponse = engineServiceBoundary.getTaskResponse(userToken, taskInfo);

        //then
        assertThat(taskResponse, is(notNullValue()));
        assertThat(taskResponse.getId(), is(subjectId));
    }

    @Test(expected = IllegalStateException.class)
    public void getAutocompleteWithoutObjetModelInCurrentState() throws Exception {
        //given
        String processName = "Process Model";
        String stateName = "Test Function";

        Long modelId = 1l;
        Long processId = 3l;
        Long subjectId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        ObjectModel objectModel = spyObjectModel(1l, processModel, "Object Model");
        SimpleAttributeModel attributeModel = spySimpleAttributeModel(2l, objectModel, "StringField", FieldType.STRING, 0);

        //create subject without user!
        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, null);
        FunctionState functionState = spyFunctionState(5l, userSubjectModel, stateName);

        userSubject.setCurrentState(functionState);
        when(subjectService.findById(subjectId)).thenReturn(Optional.of(userSubject));

        //when
        TaskInfo taskInfo = new TaskInfo(subjectId, processId, processName, stateName, LocalDateTime.MAX);
        AutocompleteResponse autocompleteResponse = engineServiceBoundary.getAutocompleteResponse(userToken, taskInfo, ObjectRequest.of(objectModel.getId()),  "Query-String");

        //then
        assertThat(autocompleteResponse, is(notNullValue()));
//        assertThat(autocompleteResponse.getId(), is(subjectId));
    }

    @Test
    public void getAutocompleteResponseSuccessful() throws Exception {
        //given
        String processName = "Process Model";
        String stateName = "Test Function";

        Long modelId = 1l;
        Long processId = 3l;
        Long subjectId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(modelId, processName);
        ProcessInstance processInstance = spyProcessInstance(processId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        ObjectModel objectModel = spyObjectModel(1l, processModel, "Object Model");
        SimpleAttributeModel attributeModel = spySimpleAttributeModel(2l, objectModel, "StringField", FieldType.STRING, 0);

        //create subject without user!
        UserSubject userSubject = spyUserSubject(subjectId, processInstance, userSubjectModel, null);
        FunctionState functionState = spyFunctionState(5l, userSubjectModel, stateName);
        functionState.addStatePermission(attributeModel, Permission.READ);

        userSubject.setCurrentState(functionState);
        when(subjectService.findById(subjectId)).thenReturn(Optional.of(userSubject));

        when(validationProviderManager.getAutocompleteProvider()).thenReturn(Arrays.asList(new AutocompleteProvider() {
            @Override
            public List<SourceMap> getAutocomplete(ObjectSchema objectSchema, String queryString) {
                return Collections.emptyList();
            }

        }));

        //when
        TaskInfo taskInfo = new TaskInfo(subjectId, processId, processName, stateName, LocalDateTime.MAX);
        AutocompleteResponse autocompleteResponse = engineServiceBoundary.getAutocompleteResponse(userToken, taskInfo, ObjectRequest.of(objectModel.getId()), "Query-String");

        //then
        assertThat(autocompleteResponse, is(notNullValue()));
//        assertThat(autocompleteResponse.getId(), is(subjectId));
    }

    @Test(expected = UserNotFoundException.class)
    public void executeTaskWithWrongUserId() throws Exception {
        //given
        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        TaskRequest taskRequest = new TaskRequest();

        //when
        Boolean result = engineServiceBoundary.executeTask(userToken, taskRequest);

        //then
        fail("executeTask with wrong 'userId' must throw IllegalArgumentException but was " + result);
    }

    @Test(expected = IllegalStateException.class)
    public void executeTaskWithWrongUserSubjectUser() throws Exception {
        //given
        Long pmId = 1l;
        Long userId = 2l;
        Long piId = 3l;
        Long sId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(pmId, "Process Model");
        ProcessInstance processInstance = spyProcessInstance(piId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        //create userSubject with other User
        User userSubjectUser = spyUser(10l, "username-1", "firstname-1", "lastname-1");
        UserSubject userSubject = spyUserSubject(sId, processInstance, userSubjectModel, userSubjectUser);
        userSubject.setCurrentState(spyFunctionState(5l, userSubjectModel, "Function"));
        when(userSubjectService.retrieveForWrite(sId)).thenReturn(Optional.of(userSubject));

        //when
        TaskRequest taskRequest = new TaskRequest(sId, NextState.of(Long.MIN_VALUE, "doesn't exists"), LocalDateTime.MIN);
        Boolean result = engineServiceBoundary.executeTask(userToken, taskRequest);

        //then
        fail("executeTask with wrong user for userSubject must throw IllegalStateException but was " + result);
    }

    @Test(expected = NullPointerException.class)
    public void executeTaskWithoutTaskRequest() throws Exception {
        //given
        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        //when
        Boolean result = engineServiceBoundary.executeTask(userToken, null);

        //then
        fail("executeTask without 'TaskRequest' must throw Exception but was " + result);
    }

    @Test(expected = TaskOutOfDateException.class)
    public void executeTaskWithOutOfDateTaskRequest() throws Exception {
        //given
        Long pmId = 1l;
        Long piId = 3l;
        Long sId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(pmId, "Process Model");
        ProcessInstance processInstance = spyProcessInstance(piId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        //create subject without user!
        UserSubject userSubject = spyUserSubject(sId, processInstance, userSubjectModel, null);
        userSubject.setCurrentState(spyFunctionState(5l, userSubjectModel, "Function"));
        when(userSubjectService.retrieveForWrite(sId)).thenReturn(Optional.of(userSubject));

        //when
        TaskRequest taskRequest = new TaskRequest(sId, NextState.of(Long.MIN_VALUE, "doesn't exists"), LocalDateTime.MIN);
        Boolean result = engineServiceBoundary.executeTask(userToken, taskRequest);

        //then
        fail("executeTask with out-dated TaskRequest must throw TaskOutOfDateException but was " + result);
    }

    @Test
    public void executeTaskSuccessful() throws Exception {
        //given
        Long pmId = 1l;
        Long piId = 3l;
        Long sId = 4l;

        UserToken userToken = UserToken.of(1L, "User Name", Collections.emptySet());
        User user = spyUser(1L, "User Name", "First Name", "Last Name");
        when(userService.findById(anyLong())).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(pmId, "Process Model");
        ProcessInstance processInstance = spyProcessInstance(piId, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));

        //create subject without user!
        UserSubject userSubject = spyUserSubject(sId, processInstance, userSubjectModel, null);
        userSubject.setCurrentState(spyFunctionState(5l, userSubjectModel, "Function"));
        when(userSubjectService.retrieveForWrite(sId)).thenReturn(Optional.of(userSubject));

        when(stateChangeService.changeState(any(Subject.class), any(TaskRequest.class))).thenReturn(true);

        //when
        TaskRequest taskRequest = new TaskRequest(sId, NextState.of(Long.MIN_VALUE, "doesn't exists"), LocalDateTime.MAX);
        Boolean result = engineServiceBoundary.executeTask(userToken, taskRequest);

        //then
        assertThat(result, is(true));
    }

}
