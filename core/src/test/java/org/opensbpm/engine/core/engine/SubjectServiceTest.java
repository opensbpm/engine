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

import org.opensbpm.engine.core.engine.SubjectService;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.EngineEventPublisher;
import org.opensbpm.engine.core.engine.SubjectService.SubjectRepository;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.UserSubject;

import static org.opensbpm.engine.core.junit.MockData.spyFunctionState;
import static org.opensbpm.engine.core.junit.MockData.spyProcessInstance;
import static org.opensbpm.engine.core.junit.MockData.spyUserSubject;
import static org.opensbpm.engine.core.junit.MockData.spyWithId;

import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.engine.entities.User;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.MockitoJUnitRunner;

/**
 * Spring Mock Unit-Test
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EngineEventPublisher eventPublisher;

    @InjectMocks
    private SubjectService subjectService;

    @Test(expected = IllegalStateException.class)
    public void createSubjectWithoutStartStateFails() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        User user = new User("username");
        SubjectModel instance = processModel.addUserSubjectModel("name", Arrays.asList(new Role("role")));
        ProcessInstance processInstance = new ProcessInstance(processModel, user);

        //when
        Subject result = subjectService.createSubject(processInstance, instance, null);

        //then
        fail("createSubject with SubjectModel without start-State must throw Exception, but was " + result);
    }

    @Test
    public void createSubjectWithStartStateSucceed() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        User user = new User("username");
        SubjectModel subjectModel = processModel.addUserSubjectModel("name", Arrays.asList(new Role("role")));

        FunctionState startState = subjectModel.addFunctionState("start");
        startState.setEventType(StateEventType.START);

        FunctionState endState = subjectModel.addFunctionState("end");
        endState.setEventType(StateEventType.END);
        startState.addHead(endState);

        ProcessInstance processInstance = spyProcessInstance(1l, processModel, user);

        when(subjectRepository.save(any(Subject.class))).thenAnswer(iom
                -> spyWithId(1l, (UserSubject) iom.getArgument(0)));

        //when
        Subject result = subjectService.createSubject(processInstance, subjectModel, null);

        //then
        assertNotNull(result);
        assertThat(result.getProcessInstance(), is(processInstance));
    }

    @Test
    public void updateStateWithoutPreviousState() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        User user = new User("username");
        Role role = new Role("role");
        role.addUser(user);
        SubjectModel subjectModel = processModel.addUserSubjectModel("name", Arrays.asList(role));

        FunctionState startState = subjectModel.addFunctionState("start");
        startState.setEventType(StateEventType.START);

        FunctionState endState = subjectModel.addFunctionState("end");
        endState.setEventType(StateEventType.END);
        startState.addHead(endState);

        ProcessInstance processInstance = spyProcessInstance(1l, processModel, user);
        Subject subject = spyUserSubject(1l, processInstance, subjectModel, user);

        when(subjectRepository.save(any(Subject.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        //TODO List<EngineEvent<?>> engineEvents = setUpEventPublisher();
        //when
        subjectService.updateState(subject, startState);

        //then
        //TODO assert EngineEvents
        assertThat(subject.getCurrentState(), is(startState));
    }

    @Test
    public void updateStateWithPreviousState() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        final User user = new User("username");
        final Role role = new Role("role");
        role.addUser(user);
        SubjectModel subjectModel = processModel.addUserSubjectModel("name", Arrays.asList(role));

        FunctionState startState = spyWithId(1L, subjectModel.addFunctionState("start"));
        startState.setEventType(StateEventType.START);

        FunctionState nextState = spyFunctionState(2L, subjectModel, "next");
        startState.addHead(nextState);

        ProcessInstance processInstance = spyProcessInstance(1L, processModel, user);
        Subject subject = spyUserSubject(1L, processInstance, subjectModel, user);
        subject.setCurrentState(startState);

        when(subjectRepository.save(any(Subject.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        //TODO List<EngineEvent<?>> engineEvents = setUpEventPublisher();
        //when
        subjectService.updateState(subject, nextState);

        //then
        verify(eventPublisher, times(1)).fireSubjectStateChanged(any(Subject.class), any(FunctionState.class), eq(Type.DELETE));
        assertThat(subject.getCurrentState(), is(nextState));
    }

}
