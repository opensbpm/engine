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

import org.junit.Test;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.builder.ObjectBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.UserSubjectBuilder;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.junit.TestTask;
import org.opensbpm.engine.core.junit.UserProcessController;
import org.opensbpm.engine.core.junit.UserProcessController.ProcessInstanceController;
import org.opensbpm.engine.core.junit.WorkflowTestCase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.opensbpm.engine.api.junit.AuditTrailMatchers.isTrail;
import static org.opensbpm.engine.api.junit.CommonMatchers.isEmpty;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.hasSubjects;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isState;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isSubjectState;
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.hasOneState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.field;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.object;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.permission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.receiveState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.sendState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

/**
 * Workflow Integration-Test for basic cases.
 */
public class ExecuteTaskIT extends WorkflowTestCase {

    /**
     * assert states for Send cascade, Function-Send-Send-Function
     *
     * @throws TaskOutOfDateException
     */
    @Test
    public void executeSendCascade() throws Exception {
        //given

        String sendRole = "Send Role";
        String receive1Role = "Receive1 Role";
        String receive2Role = "Receive2 Role";

        FieldBuilder receive1Field = field("Field", FieldType.STRING);
        ObjectBuilder receive1Message = object("Receive1 Message")
                .addAttribute(receive1Field);

        FieldBuilder receive2Field = field("Field", FieldType.STRING);
        ObjectBuilder receive2Message = object("Receive2 Message")
                .addAttribute(receive2Field);

        UserSubjectBuilder receive1Subject = userSubject("Receive1", receive1Role)
                .addState(receiveState("Wait for Message").asStart()
                        .toHead(receive1Message, functionState("Receive from Sender")
                                .addPermission(permission(receive1Message)
                                        .addPermission(receive1Field, Permission.READ, true)
                                )
                                .toHead(functionState("End").asEnd()))
                );

        UserSubjectBuilder receive2Subject = userSubject("Receive2", receive2Role)
                .addState(receiveState("Wait for Message").asStart()
                        .toHead(receive2Message, functionState("Receive from Sender")
                                .addPermission(permission(receive2Message)
                                        .addPermission(receive2Field, Permission.READ, true)
                                )
                                .toHead(functionState("End").asEnd()))
                );

        UserSubjectBuilder sendSubject = userSubject("Send", sendRole)
                .asStarter()
                .addState(functionState("Start").asStart()
                        .addPermission(permission(receive1Message)
                                .addPermission(receive1Field, Permission.WRITE, true)
                        )
                        .addPermission(permission(receive2Message)
                                .addPermission(receive2Field, Permission.WRITE, true)
                        )
                        .toHead(sendState("Inform Receive1", receive1Subject, receive1Message)
                                .toHead(sendState("Inform Receive2", receive2Subject, receive2Message)
                                        .toHead(functionState("End").asEnd())
                                )
                        )
                );

        ProcessDefinition processDefinition = process("Process")
                .addSubject(sendSubject)
                .addSubject(receive1Subject)
                .addSubject(receive2Subject)
                .addObject(receive1Message)
                .addObject(receive2Message)
                .build();

        ProcessModelInfo modelInfo = modelService.save(processDefinition);

        UserProcessController sendUser = createUserController("Send User", sendRole);
        UserProcessController receive1User = createUserController("Receive1 User", receive1Role);
        UserProcessController receive2User = createUserController("Receive2 User", receive2Role);

        ProcessInstanceController processController = sendUser.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Send", "Start", StateFunctionType.FUNCTION)
        ));
        sendUser.assertTasks(hasOneState("Start"));
        receive1User.assertTasks(isEmpty());
        receive2User.assertTasks(isEmpty());

        //when
        {
            TestTask task = sendUser.getTask("Start");
            task.setValue("Receive1 Message", "Field", "Receive1 from Starter");
            task.setValue("Receive2 Message", "Field", "Receive2 from Starter");
            sendUser.execute(task, "Inform Receive1");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Send", "Inform Receive1", StateFunctionType.SEND),
                    isSubjectState("Receive1", "Wait for Message", StateFunctionType.RECEIVE)
            ));
            sendUser.assertTasks(isEmpty());
            receive1User.assertTasks(hasOneState("Receive from Sender"));
            receive2User.assertTasks(isEmpty());
        }

        {
            TestTask task = receive1User.getTask("Receive from Sender");
            assertThat(task.getValue("Receive1 Message", "Field"), is("Receive1 from Starter"));
            receive1User.execute(task, "End");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Send", "Inform Receive2", StateFunctionType.SEND),
                    isSubjectState("Receive1", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive2", "Wait for Message", StateFunctionType.RECEIVE)
            ));
            sendUser.assertTasks(isEmpty());
            receive1User.assertTasks(isEmpty());
            receive2User.assertTasks(hasOneState("Receive from Sender"));
        }

        {
            TestTask task = receive2User.getTask("Receive from Sender");
            assertThat(task.getValue("Receive2 Message", "Field"), is("Receive2 from Starter"));
            receive2User.execute(task, "End");

            processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                    isSubjectState("Send", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive1", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive2", "End", StateFunctionType.FUNCTION)
            ));
            sendUser.assertTasks(isEmpty());
            receive1User.assertTasks(isEmpty());
            receive2User.assertTasks(isEmpty());
        }

        //then
        processController.assertTrail(contains(
                isTrail("Send", "Start"),
                isTrail("Send", "Inform Receive1"),
                isTrail("Receive1", "Wait for Message"),
                isTrail("Receive1", "Receive from Sender"),
                isTrail("Send", "Inform Receive2"),
                isTrail("Receive2", "Wait for Message"),
                isTrail("Receive1", "End"),
                isTrail("Receive2", "Receive from Sender"),
                isTrail("Send", "End"),
                isTrail("Receive2", "End")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    /**
     * assert states for asynchron Send cascade, Function-Send-Send-Function
     *
     * @throws TaskOutOfDateException
     */
    @Test
    public void executeSendCascadeAsync() throws Exception {
        //given

        String sendRole = "Send Role";
        String receive1Role = "Receive1 Role";
        String receive2Role = "Receive2 Role";

        FieldBuilder receive1Field = field("Field", FieldType.STRING);
        ObjectBuilder receive1Message = object("Receive1 Message")
                .addAttribute(receive1Field);

        FieldBuilder receive2Field = field("Field", FieldType.STRING);
        ObjectBuilder receive2Message = object("Receive2 Message")
                .addAttribute(receive2Field);

        UserSubjectBuilder receive1Subject = userSubject("Receive1", receive1Role)
                .addState(receiveState("Wait for Message").asStart()
                        .toHead(receive1Message, functionState("Receive from Sender")
                                .addPermission(permission(receive1Message)
                                        .addPermission(receive1Field, Permission.READ, true)
                                )
                                .toHead(functionState("End").asEnd()))
                );

        UserSubjectBuilder receive2Subject = userSubject("Receive2", receive2Role)
                .addState(receiveState("Wait for Message").asStart()
                        .toHead(receive2Message, functionState("Receive from Sender")
                                .addPermission(permission(receive2Message)
                                        .addPermission(receive2Field, Permission.READ, true)
                                )
                                .toHead(functionState("End").asEnd()))
                );

        UserSubjectBuilder sendSubject = userSubject("Send", sendRole)
                .asStarter()
                .addState(functionState("Start").asStart()
                        .addPermission(permission(receive1Message)
                                .addPermission(receive1Field, Permission.WRITE, true)
                        )
                        .addPermission(permission(receive2Message)
                                .addPermission(receive2Field, Permission.WRITE, true)
                        )
                        .toHead(sendState("Inform Receive1", receive1Subject, receive1Message).asAsync()
                                .toHead(sendState("Inform Receive2", receive2Subject, receive2Message).asAsync()
                                        .toHead(functionState("After Send")
                                                .toHead(functionState("End").asEnd())
                                        )
                                )
                        )
                );

        ProcessDefinition processDefinition = process("Process")
                .addSubject(sendSubject)
                .addSubject(receive1Subject)
                .addSubject(receive2Subject)
                .addObject(receive1Message)
                .addObject(receive2Message)
                .build();
        ProcessModelInfo modelInfo = modelService.save(processDefinition);

        //System.out.println(JaxbUtils.toXmlString(modelService.retrieveDefinition(modelInfo)));
        UserProcessController sendUser = createUserController("Send User", sendRole);
        UserProcessController receive1User = createUserController("Receive1 User", receive1Role);
        UserProcessController receive2User = createUserController("Receive2 User", receive2Role);

        ProcessInstanceController processController = sendUser.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Send", "Start", StateFunctionType.FUNCTION)
        ));
        sendUser.assertTasks(hasOneState("Start"));
        receive1User.assertTasks(isEmpty());
        receive2User.assertTasks(isEmpty());

        //when
        {
            TestTask task = sendUser.getTask("Start");
            task.setValue("Receive1 Message", "Field", "Receive1 from Starter");
            task.setValue("Receive2 Message", "Field", "Receive2 from Starter");
            sendUser.execute(task, "Inform Receive1");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Send", "After Send", StateFunctionType.FUNCTION),
                    isSubjectState("Receive1", "Wait for Message", StateFunctionType.RECEIVE),
                    isSubjectState("Receive2", "Wait for Message", StateFunctionType.RECEIVE)
            ));
            sendUser.assertTasks(hasOneState("After Send"));
            receive1User.assertTasks(hasOneState("Receive from Sender"));
            receive2User.assertTasks(hasOneState("Receive from Sender"));
        }

        {
            TestTask task = receive1User.getTask("Receive from Sender");
            assertThat(task.getValue("Receive1 Message", "Field"), is("Receive1 from Starter"));
            receive1User.execute(task, "End");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Send", "After Send", StateFunctionType.FUNCTION),
                    isSubjectState("Receive1", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive2", "Wait for Message", StateFunctionType.RECEIVE)
            ));
            sendUser.assertTasks(hasOneState("After Send"));
            receive1User.assertTasks(isEmpty());
            receive2User.assertTasks(hasOneState("Receive from Sender"));
        }

        {
            TestTask task = receive2User.getTask("Receive from Sender");
            assertThat(task.getValue("Receive2 Message", "Field"), is("Receive2 from Starter"));
            receive2User.execute(task, "End");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Send", "After Send", StateFunctionType.FUNCTION),
                    isSubjectState("Receive1", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive2", "End", StateFunctionType.FUNCTION)
            ));
            sendUser.assertTasks(hasOneState("After Send"));
            receive1User.assertTasks(isEmpty());
            receive2User.assertTasks(isEmpty());
        }

        {
            TestTask task = sendUser.getTask("After Send");
            sendUser.execute(task, "End");

            processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                    isSubjectState("Send", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive1", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Receive2", "End", StateFunctionType.FUNCTION)
            ));
            sendUser.assertTasks(isEmpty());
            receive1User.assertTasks(isEmpty());
            receive2User.assertTasks(isEmpty());
        }

        //then
        processController.assertTrail(contains(
                isTrail("Send", "Start"),
                isTrail("Send", "Inform Receive1"),
                isTrail("Receive1", "Wait for Message"),
                isTrail("Send", "Inform Receive2"),
                isTrail("Receive2", "Wait for Message"),
                isTrail("Send", "After Send"),
                isTrail("Receive1", "Receive from Sender"),
                isTrail("Receive1", "End"),
                isTrail("Receive2", "Receive from Sender"),
                isTrail("Receive2", "End"),
                isTrail("Send", "End")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    /**
     * assert states for case Receive-Receive-Function
     *
     * @throws TaskOutOfDateException
     */
    @Test
    public void executeCascadeReceive() throws Exception {
        //given

        String starterRole = "Starter Role";
        String senderRole = "Sender Role";
        String receiverRole = "Receiver Role";

        FieldBuilder starterField = field("Field", FieldType.STRING);
        ObjectBuilder starterMessage = object("Starter Message")
                .addAttribute(starterField);

        FieldBuilder senderField = field("Field", FieldType.STRING);
        ObjectBuilder senderMessage = object("Sender Message")
                .addAttribute(senderField);

        UserSubjectBuilder receiverSubject = userSubject("Receiver", receiverRole)
                .addState(receiveState("Receive from Starter").asStart()
                        .toHead(starterMessage, receiveState("Receive from Sender")
                                .toHead(senderMessage, functionState("After Receive")
                                        .addPermission(permission(starterMessage)
                                                .addPermission(starterField, Permission.WRITE, true)
                                        )
                                        .addPermission(permission(senderMessage)
                                                .addPermission(senderField, Permission.WRITE, true)
                                        )
                                        .toHead(functionState("End").asEnd())))
                );

        UserSubjectBuilder sendSubject = userSubject("Sender", senderRole)
                .addState(receiveState("Receive from Starter").asStart()
                        .toHead(starterMessage, functionState("Start")
                                .addPermission(permission(senderMessage)
                                        .addPermission(senderField, Permission.WRITE, true)
                                )
                                .toHead(sendState("Send Function", receiverSubject, senderMessage).asEnd()))
                );

        //possible deadlock if "Inform Receiver" is not async
        UserSubjectBuilder startSubject = userSubject("Starter", starterRole)
                .asStarter()
                .addState(functionState("Start").asStart()
                        .addPermission(permission(starterMessage)
                                .addPermission(starterField, Permission.WRITE, true)
                        )
                        .toHead(sendState("Inform Receiver", receiverSubject, starterMessage).asAsync()
                                .toHead(sendState("Inform Sender", sendSubject, starterMessage)
                                        .toHead(functionState("After Send")
                                                .toHead(functionState("End").asEnd())
                                        )
                                )
                        )
                );

        ProcessDefinition processDefinition = process("Process")
                .addSubject(startSubject)
                .addSubject(sendSubject)
                .addSubject(receiverSubject)
                .addObject(starterMessage)
                .addObject(senderMessage)
                .build();

        ProcessModelInfo modelInfo = modelService.save(processDefinition);

        UserProcessController startUser = createUserController("Start User", starterRole);
        UserProcessController senderUser = createUserController("Sender User", senderRole);
        UserProcessController receiverUser = createUserController("Receiver User", receiverRole);

        ProcessInstanceController processController = startUser.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Starter", "Start", StateFunctionType.FUNCTION)
        ));
        startUser.assertTasks(hasOneState("Start"));
        senderUser.assertTasks(isEmpty());
        receiverUser.assertTasks(isEmpty());

        //when
        {
            TestTask task = startUser.getTask("Start");
            task.setValue("Starter Message", "Field", "Starter");
            startUser.execute(task, "Inform Receiver");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Starter", "Inform Sender", StateFunctionType.SEND),
                    isSubjectState("Receiver", "Receive from Starter", StateFunctionType.RECEIVE),
                    isSubjectState("Sender", "Receive from Starter", StateFunctionType.RECEIVE)
            ));
            startUser.assertTasks(isEmpty());
            senderUser.assertTasks(hasOneState("Start"));
            receiverUser.assertTasks(isEmpty());
        }

        {
            TestTask task = senderUser.getTask("Start");
            task.setValue("Sender Message", "Field", "Sender");
            senderUser.execute(task, "Send Function");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Starter", "After Send", StateFunctionType.FUNCTION),
                    isSubjectState("Sender", "Send Function", StateFunctionType.SEND),
                    isSubjectState("Receiver", "Receive from Starter", StateFunctionType.RECEIVE)
            ));
            startUser.assertTasks(hasOneState("After Send"));
            senderUser.assertTasks(isEmpty());
            receiverUser.assertTasks(hasOneState("After Receive"));
        }

        {
            TestTask task = receiverUser.getTask("After Receive");
            assertThat(task.getValue("Starter Message", "Field"), is("Starter"));
            assertThat(task.getValue("Sender Message", "Field"), is("Sender"));
            receiverUser.execute(task, "End");

            processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                    isSubjectState("Starter", "After Send", StateFunctionType.FUNCTION),
                    isSubjectState("Sender", "Send Function", StateFunctionType.SEND),
                    isSubjectState("Receiver", "End", StateFunctionType.FUNCTION)
            ));
            startUser.assertTasks(hasOneState("After Send"));
            senderUser.assertTasks(isEmpty());
            receiverUser.assertTasks(isEmpty());
        }

        {
            TestTask task = startUser.getTask("After Send");
            startUser.execute(task, "End");

            processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                    isSubjectState("Starter", "End", StateFunctionType.FUNCTION),
                    isSubjectState("Sender", "Send Function", StateFunctionType.SEND),
                    isSubjectState("Receiver", "End", StateFunctionType.FUNCTION)
            ));
            startUser.assertTasks(isEmpty());
            senderUser.assertTasks(isEmpty());
            receiverUser.assertTasks(isEmpty());
        }

        //then
        processController.assertTrail(contains(
                isTrail("Starter", "Start"),
                isTrail("Starter", "Inform Receiver"),
                isTrail("Receiver", "Receive from Starter"),
                isTrail("Starter", "Inform Sender"),
                isTrail("Sender", "Receive from Starter"),
                isTrail("Sender", "Start"),
                isTrail("Starter", "After Send"),
                isTrail("Sender", "Send Function"),
                isTrail("Receiver", "Receive from Sender"),
                //FIXME missing isTrail("Receiver", "After Receive")
                isTrail("Receiver", "End"),
                isTrail("Starter", "End")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

}
