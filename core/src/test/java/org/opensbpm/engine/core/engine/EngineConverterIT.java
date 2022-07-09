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
package org.opensbpm.engine.core.engine;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.builder.ObjectBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToManyBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToOneBuilder;
import org.opensbpm.engine.api.model.definition.Occurs;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.EngineServiceBoundary;
import org.opensbpm.engine.core.ModelServiceBoundary;
import org.opensbpm.engine.core.UserTokenServiceBoundary;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ServiceSubject;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.hasSchemas;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isFieldSchema;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isNestedSchema;
import static org.opensbpm.engine.api.junit.TaskResponseMatcher.isObjectSchema;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.field;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.object;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.permission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toMany;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toOne;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;
import static org.opensbpm.engine.core.junit.MockData.spyFunctionState;

public class EngineConverterIT extends ServiceITCase {

    @Autowired
    private UserTokenServiceBoundary authenticationService;

    @Autowired
    private ModelServiceBoundary modelService;

    @Autowired
    private EngineServiceBoundary engineService;

    @Autowired
    private EngineConverter engineConverter;

    @Autowired
    private SubjectService subjectService;

    private UserToken userToken;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        userToken = doInTransaction(()
                -> authenticationService.registerUser(createTokenRequest("Test")));
    }

    @Test
    public void createTaskResponse() throws Exception {
        //given
        ObjectBuilder object1 = object("Object 1");
        FieldBuilder stringField = field("String Field", FieldType.STRING);
        ToOneBuilder toOneField = toOne("To One");
        FieldBuilder toOneStringField = field("String Field", FieldType.STRING);
        ToManyBuilder toManyField = toMany("To Many");
        FieldBuilder toManyStringField = field("String Field", FieldType.STRING);

        ProcessDefinition processDefinition = process("Process")
                .addSubject(userSubject("Subject", "role1").asStarter()
                        .addState(functionState("Start").asStart()
                                .addPermission(permission(object1)
                                        .addWritePermission(stringField, true)
                                        .addWritePermission(toOneField, true)
                                        .addWritePermission(toOneStringField, true)
                                        .addWritePermission(toManyField, true)
                                        .addWritePermission(toManyStringField, true)
                                )
                                .toHead(functionState("End").asEnd())
                        )
                )
                .addObject(object1
                        .addAttribute(stringField)
                        .addAttribute(toOneField
                                .addAttribute(toOneStringField))
                        .addAttribute(toManyField
                                .addAttribute(toManyStringField))
                ).build();

        Long piId = doInTransaction(() -> {
            ProcessModelInfo modelInfo = modelService.save(processDefinition);
            return engineService.startProcess(userToken, modelInfo).getProcessId();
        });

        TaskInfo taskInfo = engineService.getTasks(userToken).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no tasks for user " + userToken));

        //when
        TaskResponse result = doInTransaction(()
                -> {
            Subject subject = subjectService.findById(taskInfo.getId())
                    .orElseThrow(() -> new IllegalStateException("subject not found"));
            return engineConverter.createTaskResponse(subject);
        });

        //then
        assertThat(result, is(notNullValue()));
        assertThat("wrong document-schemas", result, hasSchemas(
                isObjectSchema("Object 1",
                        isFieldSchema("String Field", FieldType.STRING, true, false),
                        isNestedSchema("To One", Occurs.ONE, isFieldSchema("String Field", FieldType.STRING, true, false)),
                        isNestedSchema("To Many", Occurs.UNBOUND, isFieldSchema("String Field", FieldType.STRING, true, false))
                )
        ));
//        assertThat("wrong document-data", result.getDocumentResponse(), hasDatas(
//                isObjectData("Object 1")
//        ));

    }

    @Test
    public void testEvaluateDisplayNameWithObjectModel() throws Exception {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        ObjectModel objectModel = spy(new ObjectModel("model"));
        when(objectModel.getId()).thenReturn(1l);
        processModel.addObjectModel(objectModel);

        SimpleAttributeModel attributeModel = spy(new SimpleAttributeModel(objectModel, "StringField", FieldType.STRING));
        when(attributeModel.getId()).thenReturn(2l);
        objectModel.addAttributeModel(attributeModel);

        ProcessInstance processInstance = new ProcessInstance(processModel, new User("username"));

        Subject subject = new ServiceSubject(processInstance, new ServiceSubjectModel("name"));
        FunctionState state = spyFunctionState(1l, subject.getSubjectModel(), "Function");
        when(state.hasAnyPermission(any(AttributeModel.class))).thenReturn(Boolean.TRUE);
        state.setDisplayName("Text ${model.StringField} with Groovy");

        //when
        String evaluted = engineConverter.evaluteStateDisplayName(subject, state);

        //then
        assertThat(evaluted, is("Text null with Groovy"));
    }

    @Test
    public void testEvaluateDisplayNameWithObjectInstance() throws Exception {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        ObjectModel objectModel = spy(new ObjectModel("model"));
        when(objectModel.getId()).thenReturn(1l);
        processModel.addObjectModel(objectModel);

        SimpleAttributeModel attributeModel = spy(new SimpleAttributeModel(objectModel, "StringField", FieldType.STRING));
        when(attributeModel.getId()).thenReturn(2l);
        objectModel.addAttributeModel(attributeModel);

        ProcessInstance processInstance = new ProcessInstance(processModel, new User("username"));

        Map<Long, Serializable> values = new HashMap<>();
        values.put(attributeModel.getId(), "X");        
        processInstance.addObjectInstance(objectModel).setValue(values);

        Subject subject = new ServiceSubject(processInstance, new ServiceSubjectModel("name"));
        FunctionState state = spyFunctionState(1l, subject.getSubjectModel(), "Function");
        when(state.hasAnyPermission(any(AttributeModel.class))).thenReturn(Boolean.TRUE);
        state.setDisplayName("Text ${model.StringField} with Groovy");

        //when
        String evaluted = engineConverter.evaluteStateDisplayName(subject, state);

        //then
        assertThat(evaluted, is("Text X with Groovy"));
    }

}
