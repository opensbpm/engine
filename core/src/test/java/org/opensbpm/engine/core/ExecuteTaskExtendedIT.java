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

import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;

import static org.opensbpm.engine.api.junit.AuditTrailMatchers.isTrail;
import static org.opensbpm.engine.api.junit.CommonMatchers.isEmpty;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isProviderTaskChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserTaskChangedEvent;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.hasSubjects;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isState;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isSubjectState;
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.hasOneState;

import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelInfo;

import static org.opensbpm.engine.api.model.builder.DefinitionFactory.field;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.object;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.permission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.receiveState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.sendState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.serviceSubject;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

import org.opensbpm.engine.api.model.builder.ObjectBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.SendStateBuilder;
import org.opensbpm.engine.api.model.builder.ServiceSubjectBuilder;
import org.opensbpm.engine.api.model.builder.UserSubjectBuilder;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.engine.taskprovider.GroovyTaskProvider;
import org.opensbpm.engine.core.junit.UserProcessController;
import org.opensbpm.engine.core.junit.WorkflowTestCase;
import org.junit.Test;
import org.opensbpm.engine.core.junit.TestTask;
import org.opensbpm.engine.core.junit.UserProcessController.ProcessInstanceController;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Workflow Integration-Test for special cases.
 */
public class ExecuteTaskExtendedIT extends WorkflowTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void executeTaskTwiceWithSameUser() throws Exception {
        //originial exception is wrappped in doInTransaction
        thrown.expectCause(isA(TaskOutOfDateException.class));

        //given
        ProcessModelInfo modelInfo = doInTransaction(() -> {
            ProcessDefinition processDefinition = process("Process")
                    .addSubject(userSubject("Starter", "Starter Role").asStarter()
                            .addState(functionState("Start").asStart()
                                    .toHead(functionState("Function")
                                            .toHead(functionState("End").asEnd())
                                    )
                            )
                    ).build();

            return modelService.save(processDefinition);
        });

        UserProcessController startUser = createUserController("Start User", "Starter Role");
        startUser.startProcess(modelInfo);

        TestTask task = startUser.getTask("Start");
        //1.st execution of task
        try {
            startUser.execute(task, "Function");
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            fail("exception to early: " + ex.getMessage());
        }

        //2.nd execution of task must throw TaskOutOfDateException
        startUser.execute(task, "Function");
        fail("executing the same Task twice must throw TaskOutOfDateException but was successfull");
    }

    @Test
    public void executeTaskTwiceWithDifferentUsers() throws Exception {
        thrown.expectMessage(containsString("is not user of subject"));

        //given
        ProcessModelInfo modelInfo = doInTransaction(() -> {
            FieldBuilder field = field("Field", FieldType.STRING);
            ObjectBuilder messageObject = object("Message Object")
                    .addAttribute(field);

            UserSubjectBuilder subject2 = userSubject("Subject 2", "Subject Role")
                    .addState(receiveState("Start").asStart()
                            .toHead(messageObject, functionState("Receive Function")
                                    .toHead(functionState("End").asEnd()))
                    );

            UserSubjectBuilder starterSubject = userSubject("Starter", "Starter Role")
                    .asStarter()
                    .addState(functionState("Start").asStart()
                            .addPermission(permission(messageObject)
                                    .addPermission(field, Permission.WRITE, true)
                            )
                            .toHead(sendState("Send Function", subject2, messageObject)
                                    .toHead(functionState("End").asEnd())
                            )
                    );

            ProcessDefinition processDefinition = process("Process")
                    .addSubject(starterSubject)
                    .addSubject(subject2)
                    .addObject(messageObject)
                    .build();

            return modelService.save(processDefinition);
        });

        UserProcessController startUser = createUserController("Start User", "Starter Role");
        UserProcessController subject2User1 = createUserController("Subject 2 - User 1", "Subject Role");
        UserProcessController subject2User2 = createUserController("Subject 2 - User 2", "Subject Role");

        startUser.startProcess(modelInfo);

        TestTask task = startUser.getTask("Start");
        task.setValue("Message Object", "Field", "1");
        startUser.execute(task, "Send Function");

        //Subject2 Users are still unassigned
        TestTask subject2User1Task = subject2User1.getTask("Receive Function");
        TestTask subject2User2ask = subject2User2.getTask("Receive Function");

        //Subject2 User 1
        try {
            subject2User1.execute(subject2User1Task, "End");
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            fail("exception to early: " + ex.getMessage());
        }

        //Subject2 User 2
        subject2User2.execute(subject2User2ask, "End");
        fail("executing the same Task twice must throw IllegalStateException but was successfull");

    }

    @Test
    public void executeTaskWithTwoUnassignedSubjects() throws Exception {
        //given
        ProcessModelInfo modelInfo = doInTransaction(() -> {
            FieldBuilder field = field("Field", FieldType.STRING);
            ObjectBuilder messageObject = object("Message Object")
                    .addAttribute(field);

            UserSubjectBuilder subject2 = userSubject("Subject 2", "Subject Role")
                    .addState(receiveState("Start").asStart()
                            .toHead(messageObject, functionState("Receive Function")
                                    .toHead(functionState("After Receive")
                                            .toHead(functionState("End").asEnd())
                                    ))
                    );

            UserSubjectBuilder starterSubject = userSubject("Starter", "Starter Role")
                    .asStarter()
                    .addState(functionState("Start").asStart()
                            .addPermission(permission(messageObject)
                                    .addPermission(field, Permission.WRITE, true)
                            )
                            .toHead(sendState("Send Function", subject2, messageObject)
                                    .toHead(functionState("End").asEnd())
                            )
                    );

            ProcessDefinition processDefinition = process("Process")
                    .addSubject(starterSubject)
                    .addSubject(subject2)
                    .addObject(messageObject)
                    .build();

            return modelService.save(processDefinition);
        });

        UserProcessController startUser = createUserController("Start User", "Starter Role");
        UserProcessController subject2User1 = createUserController("Subject 2 - User 1", "Subject Role");
        UserProcessController subject2User2 = createUserController("Subject 2 - User 2", "Subject Role");

        ProcessInstanceController processController = startUser.startProcess(modelInfo);

        {
            TestTask task = startUser.getTask("Start");
            task.setValue("Message Object", "Field", "1");
            startUser.execute(task, "Send Function");
        }

        //Subject2 Users are still unassigned
        subject2User1.assertTasks(hasOneState("Receive Function"));
        subject2User2.assertTasks(hasOneState("Receive Function"));

        {
            TestTask task = subject2User1.getTask("Receive Function");
            subject2User1.execute(task, "After Receive");
        }

        //User2 is assigned user of Subject2
        subject2User1.assertTasks(hasOneState("After Receive"));
        subject2User2.assertTasks(isEmpty());

        {
            TestTask task = subject2User1.getTask("After Receive");
            subject2User1.execute(task, "End");
        }

        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    @Test
    public void executeTaskRecursionWithTwoSubjects() throws Exception {
        //given
        ProcessModelInfo modelInfo = doInTransaction(() -> {
            FieldBuilder field = field("Field", FieldType.STRING);
            ObjectBuilder messageObject = object("Message Object")
                    .addAttribute(field);

            UserSubjectBuilder starterSubject = userSubject("Starter", "Starter Role")
                    .asStarter();

            UserSubjectBuilder subject2 = userSubject("Subject 2", "Subject Role")
                    .addState(receiveState("Start").asStart()
                            .toHead(messageObject, functionState("Receive Function")
                                    .toHead(sendState("Send to Starter", starterSubject, messageObject)
                                            .toHead(functionState("End").asEnd())
                                    ))
                    );

            //recursion Send->Receive->Send
            SendStateBuilder starterSend = sendState("Send Function", subject2, messageObject);

            starterSend.toHead(receiveState("Receive form Subject 2")
                    .toHead(messageObject, functionState("After Receive")
                            .toHead(starterSend)
                            .toHead(functionState("End").asEnd()))
            );

            starterSubject.addState(functionState("Start").asStart()
                    .addPermission(permission(messageObject)
                            .addPermission(field, Permission.WRITE, true)
                    )
                    .toHead(starterSend)
            );

            ProcessDefinition processDefinition = process("Process")
                    .addSubject(starterSubject)
                    .addSubject(subject2)
                    .addObject(messageObject)
                    .build();

            return modelService.save(processDefinition);
        });

        UserProcessController startUser = createUserController("Start User", "Starter Role");
        UserProcessController subject2User1 = createUserController("Subject 2 - User 1", "Subject Role");
        UserProcessController subject2User2 = createUserController("Subject 2 - User 2", "Subject Role");

        ProcessInstanceController processController = startUser.startProcess(modelInfo);

        //1st Round
        {
            TestTask startTask = startUser.getTask("Start");
            startTask.setValue("Message Object", "Field", "1");
            startUser.execute(startTask, "Send Function");

            //Subject2 Users are still unassigned
            subject2User1.assertTasks(hasOneState("Receive Function"));
            subject2User2.assertTasks(hasOneState("Receive Function"));

            //execute 1st Round time with User 1
            TestTask subject2User1Task = subject2User1.getTask("Receive Function");
            subject2User1.execute(subject2User1Task, "Send to Starter");

            //Subject2 (User 1) is finished
            subject2User1.assertTasks(isEmpty());
            subject2User2.assertTasks(isEmpty());

        }

        //2nd Round
        {
            TestTask startTask = startUser.getTask("After Receive");
            startUser.execute(startTask, "Send Function");

            //Subject2 Users are still unassigned
            subject2User1.assertTasks(hasOneState("Receive Function"));
            subject2User2.assertTasks(hasOneState("Receive Function"));

            //execute 2nd Round with User 2
            TestTask subject2User2Task = subject2User2.getTask("Receive Function");
            subject2User1.execute(subject2User2Task, "Send to Starter");

            //Subject2 (User 1) is finished
            subject2User1.assertTasks(isEmpty());
            subject2User2.assertTasks(isEmpty());
        }

        TestTask startTask = startUser.getTask("After Receive");
        startUser.execute(startTask, "End");

        processController.assertTrail(contains(
                isTrail("Starter", "Start User", "Start"),
                isTrail("Starter", "Start User", "Send Function"),
                isTrail("Subject 2", "Subject 2 - User 1", "Start"),
                isTrail("Subject 2", "Subject 2 - User 1", "Receive Function"),
                isTrail("Starter", "Start User", "Receive form Subject 2"),
                isTrail("Subject 2", "Subject 2 - User 1", "Send to Starter"),
                isTrail("Starter", "Start User", "After Receive"),
                isTrail("Subject 2", "Subject 2 - User 1", "End"),
                isTrail("Starter", "Start User", "Send Function"),
                isTrail("Subject 2", "Subject 2 - User 1", "Start"),
                isTrail("Subject 2", "Subject 2 - User 1", "Receive Function"),
                isTrail("Starter", "Start User", "Receive form Subject 2"),
                isTrail("Subject 2", "Subject 2 - User 1", "Send to Starter"),
                isTrail("Starter", "Start User", "After Receive"),
                isTrail("Subject 2", "Subject 2 - User 1", "End"),
                isTrail("Starter", "Start User", "End")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    @Test
    public void executeTaskWithServiceSubject() throws Exception {
        //given
        FieldBuilder field = field("Field", FieldType.STRING);
        ObjectBuilder messageObject = object("Message Object")
                .addAttribute(field);

        UserSubjectBuilder starterSubject = userSubject("Starter", "Starter Role")
                .asStarter();
        ServiceSubjectBuilder serviceSubject = serviceSubject("Script Service");

        starterSubject.addState(functionState("Start").asStart()
                .addPermission(permission(messageObject)
                        .addPermission(field, Permission.WRITE, true)
                )
                .toHead(sendState("Send to Service", serviceSubject, messageObject)
                        .toHead(receiveState("Receive from Service").asStart()
                                .toHead(messageObject, functionState("After Receive")
                                        .addPermission(permission(messageObject)
                                                .addPermission(field, Permission.READ, true)
                                        )
                                        .toHead(functionState("End").asEnd())
                                )
                        )
                )
        );

        serviceSubject.addState(receiveState("Receive from Starter").asStart()
                .toHead(messageObject, functionState("Script Function")
                        .withProvider(GroovyTaskProvider.NAME)
                        .addParameter(GroovyTaskProvider.SCRIPT, loadScript())
                        .addPermission(permission(messageObject)
                                .addPermission(field, Permission.WRITE, true)
                        )
                        .toHead(sendState("Send to Starter", starterSubject, messageObject).asEnd()
                        )
                )
        );

        ProcessDefinition processDefinition = process("Process")
                .addSubject(starterSubject)
                .addSubject(serviceSubject)
                .addObject(messageObject)
                .build();
        ProcessModelInfo modelInfo = modelService.save(processDefinition);

        UserProcessController startUser = createUserController("Start User", "Starter Role");

        ProcessInstanceController processController = startUser.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Starter", "Start", StateFunctionType.FUNCTION)
        ));
        startUser.assertTasks(hasOneState("Start"));

        engineEventsCollector.clear();

        {
            TestTask task = startUser.getTask("Start");
            task.setValue("Message Object", "Field", "1");
            startUser.execute(task, "Send to Service");
            assertEngineEvents(
                    isUserTaskChangedEvent(startUser.getId(), "Start", Type.DELETE),
                    isProviderTaskChangedEvent(Type.CREATE)
            );
            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Starter", "Send to Service", StateFunctionType.SEND)
            ));

        }

        //wait a little bit for Service to execute "Script Function"
        Thread.sleep(5000);
        assertEngineEvents(
                isProviderTaskChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(startUser.getId(), "After Receive", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Starter", "Receive from Service", StateFunctionType.RECEIVE)
        ));

        {
            TestTask task = startUser.getTask("After Receive");
            assertThat(task.getValue("Message Object", "Field"), is("Groovy Script"));
            startUser.execute(task, "End");
        }

        processController.assertTrail(contains(
                isTrail("Starter", "Start"),
                isTrail("Starter", "Send to Service"),
                isTrail("Script Service", "Receive from Starter"),
                isTrail("Script Service", "Script Function"),
                isTrail("Starter", "Receive from Service"),
                isTrail("Script Service", "Send to Starter"),
                isTrail("Starter", "After Receive"),
                isTrail("Starter", "Start User", "End")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    private String loadScript() throws IOException {
        String script = "/scripts/executeTaskWithServiceSubject.groovy";
        try (InputStream input = getClass().getResourceAsStream(script)) {
            return IOUtils.toString(input, "UTF-8");
        }
    }

}
