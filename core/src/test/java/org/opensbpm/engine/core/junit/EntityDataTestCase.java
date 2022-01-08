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

import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.utils.entities.HasId;
import java.util.List;

public abstract class EntityDataTestCase extends DataJpaTestCase {

    protected User persistedUser() {
        return persistedUser("userName");
    }

    protected User persistedUser(String userName) {
        return entityManager.persist(new User(userName));
    }

    protected Role persistedRole(String name) {
        return entityManager.persist(new Role(name));
    }

    protected ProcessModel persistedProcessModel() {
        return entityManager.persist(new ProcessModel("name", new ModelVersion(0, 0)));
    }

    protected ObjectModel persistedObjectModel() {
        return persistedObjectModel(persistedProcessModel());
    }

    protected ObjectModel persistedObjectModel(ProcessModel processModel) {
        return persistedObjectModel(processModel, "name");
    }

    protected ObjectModel persistedObjectModel(ProcessModel processModel, String name) {
        return entityManager.persist(processModel.addObjectModel(name));
    }

    protected UserSubjectModel persistedUserSubjectModel(ProcessModel processModel, String subject, List<Role> roles) {
        return entityManager.persist(processModel.addUserSubjectModel(subject, roles));
    }

    protected ServiceSubjectModel persistedServiceSubjectModel() {
        return persistedServiceSubjectModel(persistedProcessModel());
    }

    protected ServiceSubjectModel persistedServiceSubjectModel(ProcessModel processModel) {
        return persistedServiceSubjectModel(processModel, "subject");
    }

    protected ServiceSubjectModel persistedServiceSubjectModel(ProcessModel processModel, String subject) {
        return entityManager.persist(processModel.addServiceSubjectModel(subject));
    }

    protected FunctionState persistedFunctionState(SubjectModel subjectModel) {
        FunctionState state = subjectModel.addFunctionState("Function");
        return entityManager.persist(state);
    }

    protected ReceiveState persistedReceiveState(SubjectModel subjectModel) {
        final ReceiveState state = subjectModel.addReceiveState("Receive");
        return entityManager.persist(state);
    }

    protected ProcessInstance persistedProcessInstance(ProcessModel processModel) {
        return persistedProcessInstance(processModel, persistedUser());
    }

    protected ProcessInstance persistedProcessInstance(ProcessModel processModel, User startUser) {
        return entityManager.persist(new ProcessInstance(processModel, startUser));
    }

    @SuppressWarnings("unchecked")
    protected <T extends HasId> T find(Class<T> entityClass, T entity) {
        return entityManager.find(entityClass, entity.getId());
    }

}
