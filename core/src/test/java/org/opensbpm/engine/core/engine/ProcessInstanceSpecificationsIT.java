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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.engine.ProcessInstanceService.ProcessInstanceRepository;
import org.opensbpm.engine.core.engine.ProcessInstanceService.ProcessInstanceSpecifications;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.junit.DataJpaTestCase;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ProcessInstanceSpecificationsIT extends DataJpaTestCase {

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;

    @Test
    public void testFindAllWithState() {
        //given
        User startUser = new User("userName");
        ProcessInstance processInstance = createProcessInstance(startUser);
        assertThat(processInstance.getId(), is(notNullValue()));

        //when
        List<ProcessInstance> processInstances = processInstanceRepository.findAll(ProcessInstanceSpecifications.withState(ProcessInstanceState.ACTIVE));

        //then
        assertThat(processInstances, hasSize(1));
    }

    @Test
    public void testFindAllWithUserIdAndState() {

        //given
        User startUser = new User("userName");
        ProcessInstance processInstance = createProcessInstance(startUser);
        assertThat(processInstance.getId(), is(notNullValue()));

        //when
        List<ProcessInstance> processInstances = processInstanceRepository.findAll(ProcessInstanceSpecifications.withUserAndState(startUser, ProcessInstanceState.ACTIVE));

        //then
        assertThat(processInstances, hasSize(1));
    }

    private ProcessInstance createProcessInstance(User startUser) {
        Role role = new Role("role");
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));

        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(role));
        processModel.setStarterSubject(userSubjectModel);
        FunctionState startFunction = userSubjectModel.addFunctionState("start");
        startFunction.setEventType(StateEventType.START);

        FunctionState endFunction = userSubjectModel.addFunctionState("end");
        endFunction.setEventType(StateEventType.END);
        startFunction.addHead(endFunction);

        processModel = entityManager.persistFlushFind(processModel);

        startUser = entityManager.persist(startUser);
        ProcessInstance processInstance = entityManager.persist(new ProcessInstance(processModel, startUser));

        UserSubject userSubject = new UserSubject(processInstance, processModel.getStarterSubjectModel(), startUser);
        userSubject = entityManager.persist(userSubject);
        assertThat(userSubject.getId(), is(notNullValue()));

        return entityManager.persistAndFlush(processInstance);
    }

}
