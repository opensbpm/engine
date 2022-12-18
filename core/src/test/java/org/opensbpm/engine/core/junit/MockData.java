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
package org.opensbpm.engine.core.junit;

import java.util.Arrays;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ServiceSubject;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.core.model.entities.StatePermission;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MockData {

    public static User spyUser(long id, String name, String firstname, String lastname) {
        return spyWithId(id, new User(name));
    }

    public static Role spyRole(long id, String name) {
        return spyWithId(id, new Role(name));
    }

    public static ProcessModel spyProcessModel(long id, String name) {
        return spyWithId(id, new ProcessModel(name, new ModelVersion(0, 0)));
    }

    public static ObjectModel spyObjectModel(long id, ProcessModel processModel, String name) {
        ObjectModel objectModel = spyWithId(id, new ObjectModel(name));
        processModel.addObjectModel(objectModel);
        return objectModel;
    }

    public static SimpleAttributeModel spySimpleAttributeModel(long id, ObjectModel objectModel, String fieldName, FieldType fieldType, int position) {
        SimpleAttributeModel attributeModel = spyWithId(id, new SimpleAttributeModel(objectModel, fieldName, fieldType));
        objectModel.addAttributeModel(attributeModel);
        return attributeModel;
    }

    public static UserSubjectModel spyUserSubjectModel(long id, ProcessModel processModel, String name, Role... roles) {
        UserSubjectModel userSubjectModel = spyWithId(id, new UserSubjectModel(name, Arrays.asList(roles)));
        processModel.addSubjectModel(userSubjectModel);
        return userSubjectModel;
    }

    public static ProcessInstance spyProcessInstance(long id, ProcessModel processModel, User startUser) {
        return spyWithId(id, new ProcessInstance(processModel, startUser));
    }

    public static UserSubject spyUserSubject(long id, ProcessInstance processInstance, SubjectModel subjectModel, User user) {
        return spyWithId(id, new UserSubject(processInstance, subjectModel, user));
    }

    public static ServiceSubject spyServiceSubject(long id, ProcessInstance processInstance, SubjectModel subjectModel) {
        return spyWithId(id, new ServiceSubject(processInstance, subjectModel));
    }

    public static FunctionState spyFunctionState(long id, SubjectModel subjectModel, String name) {
        return spyWithId(id, new FunctionState(name));
    }

    public static ReceiveState spyReceiveState(long id, SubjectModel subjectModel, String name) {
        return spyWithId(id, new ReceiveState(name));
    }

    public static SendState spySendState(long id, SubjectModel receiver, ObjectModel objectModel, String name) {
        return spyWithId(id, new SendState(name, receiver, objectModel));
    }

    public static StatePermission spyStatePermission(long id, FunctionState state, SimpleAttributeModel simpleAttributeModel, Permission permission) {
        StatePermission statePermission = spyWithId(id, new StatePermission(state, simpleAttributeModel, permission));
        state.addStatePermission(statePermission);
        return statePermission;
    }

    public static <T extends HasId> T spyWithId(long id, T entity) {
        T spyEntity = spy(entity);
        when(spyEntity.getId()).thenReturn(id);
        return spyEntity;
    }

    private MockData() {
    }

}
