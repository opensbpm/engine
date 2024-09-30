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
package org.opensbpm.engine.api.model.builder;

import java.util.logging.Logger;
import org.junit.Test;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.AttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToManyBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToOneBuilder;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.containsHeads;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.containsPermisssions;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isFieldPermission;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isFunctionState;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isNestedPermission;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isPermission;
import static org.opensbpm.engine.api.junit.ModelUtils.getSubject;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isField;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isFieldWithIndex;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObject;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isStarterSubjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isSubjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isToMany;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isToOne;
import static org.opensbpm.engine.api.junit.ReceiveStateDefinitionMatchers.isReceiveState;
import static org.opensbpm.engine.api.junit.SendStateDefinitionMatchers.isSendState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.field;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.nestedPermission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.object;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.permission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.receiveState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.sendState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.serviceSubject;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toMany;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toOne;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

public class DefinitionFactoryTest {
    
    @Test
    public void testBuild() throws Exception {
        //given
        FieldBuilder field1 = field("Object 1 - Field 1", FieldType.STRING);
        ToOneBuilder toOneField = toOne("To One");
        FieldBuilder toOneStringField = field("String Field", FieldType.STRING);
        ToManyBuilder toManyField = toMany("To Many");
        FieldBuilder toManyStringField = field("String Field", FieldType.STRING);
        
        ObjectBuilder object1 = object("Object 1");
        
        FieldBuilder o2Field1 = field("Object 2 - Field 1", FieldType.STRING)
                .asIndexed();
        FieldBuilder o2Field2 = field("Object 2 - Field 2", FieldType.STRING);

        ObjectBuilder object2 = object("Object 2")
                .withDisplayName("Object 2 - Display")
                .addAttribute(o2Field1)
                .addAttribute(o2Field2);
        
        ServiceSubjectBuilder serviceSubject = serviceSubject("Service")
                .addState(receiveState("Receive").asStart()
                        .toHead(object1, functionState("Execute")
                                .withDisplayName("Execute this Function")
                                .withProvider("Provider")
                                .addParameter("MyParameter", "Value")
                                .addPermission(permission(object1)
                                        .addWritePermission(field1, true, "defaultValue")
                                        .addPermission(nestedPermission(toOneField, Permission.WRITE, true)
                                                .addPermission(new AttributePermissionBuilder(toOneStringField, Permission.WRITE, true))
                                        )
                                        .addPermission(nestedPermission(toManyField, Permission.WRITE, true)
                                                .addPermission(new AttributePermissionBuilder(toManyStringField, Permission.WRITE, true))
                                        )
                                )
                                .addPermission(permission(object2)
                                        .addPermission(o2Field1, Permission.WRITE, true)
                                        .addReadPermission(o2Field2)
                                )
                                .toHead(functionState("End").asEnd()))
                );

        //starter
        FunctionStateBuilder recurseFunction = functionState("Recurse");
        UserSubjectBuilder starter = userSubject("Starter", "Role 1")
                .asStarter().addRole("Role 2")
                .addState(functionState("Start").asStart()
                        .toHead(recurseFunction.toHead(functionState("In Recurse").toHead(recurseFunction)))
                        .toHead(sendState("Send", serviceSubject, object1).asAsync()
                                .toHead(receiveState("Receive")
                                        .toHead(object1, functionState("Do Receive")
                                                .toHead(functionState("End").asEnd()))
                                )
                        )
                );

        //create recursion with send/receivstates
        serviceSubject.addState(sendState("Send", starter, object1).asEnd());
        
        ProcessBuilder processBuilder = process("Process")
                .version(0)
                .description("Description")
                .asIncative()
                .addObject(object1
                        .addAttribute(field1)
                        .addAttribute(toOneField
                                .addAttribute(toOneStringField)
                        ).addAttribute(toManyField
                                .addAttribute(toManyStringField)
                        )
                )
                .addObject(object2)
                .addSubject(starter)
                .addSubject(serviceSubject);

        assertThat(object1.getAttribute("To Many"), instanceOf(ToManyBuilder.class));
        assertThat(((ToManyBuilder)object1.getAttribute("To Many")).getAttribute("String Field"), is(notNullValue()));
        
        //when
        ProcessDefinition processDefinition = processBuilder.build();

        //then
        Logger.getLogger(getClass().getName()).info("" + processDefinition);
        assertThat(processDefinition.getName(), is("Process"));
        assertThat(processDefinition.getVersion(), is(0));
        assertThat(processDefinition.getDescription(), is("Description"));
        assertThat(processDefinition.getState(), is(ProcessModelState.INACTIVE));
        assertThat("wrong subjects", processDefinition.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Starter"),
                        isSubjectName("Service")
                ));
        
        assertThat("wrong states for 'Starter' ", getSubject(processDefinition, "Starter").getStates(),
                containsInAnyOrder(
                        isFunctionState("Start", containsHeads(
                                isFunctionState("Recurse"),
                                isSendState("Send")
                        )),
                        isFunctionState("Recurse", containsHeads(
                                isFunctionState("In Recurse")
                        )),
                        isFunctionState("In Recurse", containsHeads(
                                isFunctionState("Recurse")
                        )),
                        isSendState("Send", "Service", "Object 1", "Receive"),
                        isReceiveState("Receive"),
                        isFunctionState("Do Receive", containsHeads(
                                isFunctionState("End")
                        )),
                        isFunctionState("End")
                ));
        
        assertThat("wrong states for 'Service' ", getSubject(processDefinition, "Service").getStates(),
                containsInAnyOrder(
                        isReceiveState("Receive"),
                        isFunctionState("Execute", "Execute this Function", containsPermisssions(
                                isPermission("Object 1",
                                        isFieldPermission("Object 1 - Field 1", Permission.WRITE, true,"defaultValue"),
                                        isNestedPermission("To One",
                                                isFieldPermission("String Field", Permission.WRITE, true)
                                        ),
                                        isNestedPermission("To Many",
                                                isFieldPermission("String Field", Permission.WRITE, true)
                                        )
                                ),
                                isPermission("Object 2",
                                        isFieldPermission("Object 2 - Field 1", Permission.WRITE, true),
                                        isFieldPermission("Object 2 - Field 2", Permission.READ, false)
                                )
                        )
                        ),
                        isFunctionState("End"),
                        isSendState("Send", "Starter", "Object 1")
                ));
        
        assertThat("wrong objects", processDefinition.getObjects(),
                containsInAnyOrder(
                        isObject("Object 1",
                                isField("Object 1 - Field 1", FieldType.STRING),
                                isToOne("To One", 
                                        isField("String Field", FieldType.STRING)
                                ),
                                isToMany("To Many", 
                                        isField("String Field", FieldType.STRING)
                                )
                        ),
                        isObject("Object 2", "Object 2 - Display",
                                isFieldWithIndex("Object 2 - Field 1", FieldType.STRING),
                                isField("Object 2 - Field 2", FieldType.STRING)
                        )
                ));
    }
    
}
