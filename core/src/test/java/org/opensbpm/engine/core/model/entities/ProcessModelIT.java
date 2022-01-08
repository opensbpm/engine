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
package org.opensbpm.engine.core.model.entities;

import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.MessageModel;
import org.opensbpm.engine.core.model.entities.StatePermission;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.junit.EntityDataTestCase;
import org.opensbpm.engine.core.model.RoleService;
import java.sql.Timestamp;
import java.util.Arrays;
import javax.persistence.PersistenceException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

import org.hibernate.exception.ConstraintViolationException;

import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ProcessModelIT extends EntityDataTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @MockBean
    private RoleService roleService;

    @Test
    public void testJpaConverter() {
        //given
        ProcessModel processModel = entityManager.persistFlushFind(new ProcessModel("process", new ModelVersion(0, 0)));

        //when
        Timestamp timestamp = (Timestamp) entityManager.getEntityManager().createNativeQuery(
                "SELECT created_At FROM process_model p where p.id = :id")
                .setParameter("id", processModel.getId())
                .getSingleResult();

        ProcessModel result = entityManager.persistFlushFind(processModel);

        //then
        assertThat(timestamp, is(notNullValue()));
    }

    @Test(expected = PersistenceException.class)
    public void testInsertEmpty() {
        //given
        ProcessModel processModel = new ProcessModel();

        //when
        ProcessModel result = entityManager.persistFlushFind(processModel);

        //then
        fail("persist without values must throw PersistenceException, but was " + result);
    }

    @Test
    public void testInsert() {
        //given
        ProcessModel processModel = new ProcessModel("process", new ModelVersion(0, 0));

        //when
        ProcessModel result = entityManager.persistFlushFind(processModel);

        //then
        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getName(), is("process"));
        assertThat(result.getVersion().getMajor(), is(0));
        assertThat(result.getVersion().getMinor(), is(0));
        assertThat(result.getDescription(), is(nullValue()));
        assertThat(result.getCreatedAt(), is(notNullValue()));
        assertThat(result.getState(), is(ProcessModelState.ACTIVE));
    }

    @Test
    public void testUniqueConstraint() {
        thrown.expectCause(isA(ConstraintViolationException.class));

        //given
        ProcessModel processModel = entityManager.persistFlushFind(new ProcessModel("process", new ModelVersion(0, 0)));
        assertThat(processModel.getId(), is(notNullValue()));

        //when
        ProcessModel result = entityManager.persistFlushFind(new ProcessModel("process", new ModelVersion(0, 0)));

        //then
        fail("persist of the same ProcessModel twice must throw exception, but was " + result);
    }

    @Test
    public void testInsertCascade() {
        //given
        AllForeignKeys foreignKeys = new AllForeignKeys();

        //when
        ProcessModel result = entityManager.persistFlushFind(foreignKeys.processModel);

        //then
        assertNotNull(result.getId());

        assertThat(result.getStarterSubjectModel(), is(notNullValue()));
        assertThat(result.getStarterSubjectModel().getName(), is("starter"));

        assertThat(result.getSubjectModels(), hasSize(2));
        assertThat(result.getSubjectModels(), hasItem(hasProperty("id", notNullValue())));

        assertThat(result.getObjectModels(), hasSize(1));
        assertThat(result.getObjectModels(), hasItem(hasProperty("id", notNullValue())));

        foreignKeys.assertForeignKeys(notNullValue());
    }

    @Ignore("doesnt' work at the moment")
    @Test
    public void testDeleteCascade() {
        //given

        //create a ProcessModel with all possible foreign-keys
        AllForeignKeys foreignKeys = new AllForeignKeys();
        ProcessModel processModel = entityManager.persistFlushFind(foreignKeys.processModel);

        //when
        entityManager.remove(processModel);
        entityManager.flush();

        //then
        assertThat(entityManager.find(ProcessModel.class, processModel.getId()), is(nullValue()));

        foreignKeys.assertForeignKeys(nullValue());
    }

    private class AllForeignKeys {

        private final ProcessModel processModel;

        private final ObjectModel objectModel;
        private final SimpleAttributeModel simpleAttributeModel;
//        private final ObjectModel childObjectModel;
//        private final SimpleAttributeModel childFieldModel;
//        private final ObjectModel leafObjectModel;
//        private final SimpleAttributeModel leafFieldModel;

        private final Role role;

        private final UserSubjectModel starterSubjectModel;
        private final FunctionState starterFunction;
        private final FunctionState starterFunction2;
        private final StatePermission function2StatePermission;

        private final SendState starterSend;
        private final FunctionState starterEnd;

        private final ServiceSubjectModel serviceSubjectModel;
        private final ReceiveState serviceReceive;
        private final FunctionState serviceFunction;
        private final StatePermission serviceFunctionObjectStatePermission;
        private final MessageModel messageModel;

        public AllForeignKeys() {
            processModel = new ProcessModel("Process", new ModelVersion(0, 0));

            objectModel = processModel.addObjectModel("object");
            simpleAttributeModel = objectModel.addAttributeModel(new SimpleAttributeModel(objectModel, "Field", FieldType.STRING));
            //childObjectModel = objectModel.addChild("Child Object");
            //childFieldModel = childObjectModel.addAttributeModel(new SimpleAttributeModel("Child Field", FieldType.STRING));
            //leafObjectModel = childObjectModel.addChild("Leaf Object");
            //leafFieldModel = leafObjectModel.addAttributeModel(new SimpleAttributeModel("Field", FieldType.STRING));

            role = new Role("role");

            starterSubjectModel = processModel.addUserSubjectModel("starter", Arrays.asList(role));
            processModel.setStarterSubject(starterSubjectModel);

            starterFunction = starterSubjectModel.addFunctionState("Starter Function");
            starterFunction.setEventType(StateEventType.START);

            starterFunction2 = starterSubjectModel.addFunctionState("Function 2");
            starterFunction.addHead(starterFunction2);
            function2StatePermission = starterFunction2.addStatePermission(simpleAttributeModel, Permission.WRITE);

            serviceSubjectModel = processModel.addServiceSubjectModel("Service");
            serviceFunction = serviceSubjectModel.addFunctionState("Service Function");
            serviceFunctionObjectStatePermission = serviceFunction.addStatePermission(simpleAttributeModel, Permission.WRITE);

            serviceReceive = serviceSubjectModel.addReceiveState("Service Receive from Starter");
            messageModel = serviceReceive.addMessageModel(objectModel, serviceFunction);

            starterSend = starterSubjectModel.addSendState("Starter Send", serviceSubjectModel, objectModel);
            starterEnd = starterSubjectModel.addFunctionState("End");
            starterSend.setHead(starterEnd);

        }

        void assertForeignKeys(Matcher<Object> entityMatcher) {

            assertThat(find(ObjectModel.class, objectModel), is(entityMatcher));
            assertThat(find(SimpleAttributeModel.class, simpleAttributeModel), is(entityMatcher));
//            assertThat(find(ObjectModel.class, childObjectModel), is(entityMatcher));
//            assertThat(find(SimpleAttributeModel.class, childFieldModel), is(entityMatcher));
//            assertThat(find(ObjectModel.class, leafObjectModel), is(entityMatcher));
//            assertThat(find(SimpleAttributeModel.class, leafFieldModel), is(entityMatcher));

            assertThat("Role must always exist", find(Role.class, role), is(notNullValue()));
            assertThat(find(UserSubjectModel.class, starterSubjectModel), is(entityMatcher));

            assertThat(find(FunctionState.class, starterFunction), is(entityMatcher));
            assertThat(find(FunctionState.class, starterFunction2), is(entityMatcher));
            assertThat(find(StatePermission.class, function2StatePermission), is(entityMatcher));

            assertThat(find(SendState.class, starterSend), is(entityMatcher));
            assertThat(find(FunctionState.class, starterEnd), is(entityMatcher));

            assertThat(find(ServiceSubjectModel.class, serviceSubjectModel), is(entityMatcher));
            assertThat(find(ReceiveState.class, serviceReceive), is(entityMatcher));
            assertThat(find(MessageModel.class, messageModel), is(entityMatcher));

            assertThat(find(FunctionState.class, serviceFunction), is(entityMatcher));
            assertThat(find(StatePermission.class, serviceFunctionObjectStatePermission), is(entityMatcher));

        }

    }
}
