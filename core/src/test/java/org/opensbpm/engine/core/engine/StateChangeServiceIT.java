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

import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.AttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.ToManyPermissionBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToManyBuilder;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.EngineServiceBoundary;
import org.opensbpm.engine.core.ModelServiceBoundary;
import org.opensbpm.engine.core.UserTokenServiceBoundary;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.junit.TestTask;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.fail;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.field;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.object;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.permission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toMany;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

public class StateChangeServiceIT extends ServiceITCase {

    @Autowired
    private ModelServiceBoundary modelService;

    @Autowired
    private EngineServiceBoundary engineService;

    @Autowired
    private StateChangeService stateChangeService;

    @Autowired
    private UserTokenServiceBoundary userTokenService;

    private UserToken userToken;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        userToken = userTokenService.registerUser(createTokenRequest("user1", "starter-role"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveRequiredWithoutValue() {
        //given
        FieldBuilder o1field1 = field("field1", FieldType.STRING);
        ObjectBuilder object1 = object("object1")
                .addAttribute(o1field1);

        FunctionStateBuilder startState = new FunctionStateBuilder("start").eventType(StateEventType.START)
                .addPermission(permission(object1)
                        .addPermission(o1field1, Permission.WRITE, true)
                );

        ProcessDefinition processDefinition = process("process")
                .addSubject(userSubject("starter", "role1").asStarter()
                        .addState(startState.toHead(
                                new FunctionStateBuilder("end").eventType(StateEventType.END)
                        )))
                .addObject(object1)
                .build();

        Long piId = doInTransaction(() -> {
            ModelRequest modelRequest = ModelRequest.of(modelService.save(processDefinition));
            return engineService.startProcess(userToken, modelRequest).getProcessId();
        });

        //when
        boolean changeStateResult = doInTransaction(() -> {
            TaskInfo taskInfo = engineService.getTasks(userToken).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no tasks for user " + userToken));
            TaskResponse taskResponse = engineService.getTaskResponse(userToken, taskInfo);
            TestTask task = new TestTask(taskInfo, taskResponse);

            task.setValue("object1", "field1", null);
            return changeState(task);
        });

        //then
        fail("save of required field without values must throw Exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveRequiredNestedWithoutValue() {
        //given
        ToManyBuilder o1field1 = toMany("Nested Field");
        FieldBuilder o1field2 = field("String Field", FieldType.STRING);
        ObjectBuilder object1 = object("object1")
                .addAttribute(o1field1
                        .addAttribute(o1field2)
                );

        FunctionStateBuilder startState = new FunctionStateBuilder("start").eventType(StateEventType.START)
                .addPermission(permission(object1)
                        .addPermission(new ToManyPermissionBuilder(o1field1, Permission.WRITE, true)
                                .addPermission(new AttributePermissionBuilder(o1field2, Permission.WRITE, true))
                        )
                );

        ProcessDefinition processDefinition = process("process")
                .addSubject(userSubject("starter", "role1").asStarter()
                        .addState(startState.toHead(
                                new FunctionStateBuilder("end").eventType(StateEventType.END)
                        )))
                .addObject(object1)
                .build();

        Long piId = doInTransaction(() -> {
            ProcessModelInfo modelInfo = modelService.save(processDefinition);
            return engineService.startProcess(userToken, ModelRequest.of(modelInfo)).getProcessId();
        });

        //when
        boolean changeStateResult = doInTransaction(() -> {
            TaskInfo taskInfo = engineService.getTasks(userToken).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("no tasks for user " + userToken));
            TaskResponse taskResponse = engineService.getTaskResponse(userToken, taskInfo);
            TestTask task = new TestTask(taskInfo, taskResponse);

            task.setValue("object1", "field1", null);
            return changeState(task);
        });

        //then
        fail("save of required field without values must throw Exception");
    }

    private boolean changeState(Task task) {
        NextState nextState = task.getNextStates().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no taskdata for user " + userToken));
        TaskRequest taskRequest = task.createTaskRequest(nextState);

        Subject subject = entityManager.find(Subject.class, task.getId());

        return stateChangeService.changeState(subject, taskRequest);
    }

}
