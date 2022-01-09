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
package org.opensbpm.engine.core.engine.entities;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.junit.EntityTestCase;
import org.opensbpm.engine.core.junit.MockData;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class ProcessInstanceTest extends EntityTestCase<ProcessInstance> {

    public ProcessInstanceTest() {
        super(ProcessInstance.class);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSetState() {
        //given
        ProcessInstanceState state = ProcessInstanceState.FINISHED;
        ProcessInstance processInstance = new ProcessInstance();

        //when
        processInstance.setState(state);

        //then
        assertThat(processInstance.getEndTime(), is(notNullValue()));
        assertThat(processInstance.isStopped(), is(true));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSubjects() {
        //given
        ProcessInstance processInstance = new ProcessInstance();

        //when
        processInstance.getSubjects().add(new UserSubject());

        //then
        fail("getSubjects must return unmodifiable Collection.");
    }

    @Test
    public void testAddSubject() {
        //given
        UserSubject subject = new UserSubject();
        ProcessInstance processInstance = new ProcessInstance();

        //when
        processInstance.addSubject(subject);

        //then
        assertThat(processInstance.getSubjects(), hasItem(subject));
        //backpointer
        assertThat(subject.getProcessInstance(), is(processInstance));
    }

    @Test
    public void testHaveActiveSubjects_withoutSubjects() {
        //given
        ProcessInstance processInstance = new ProcessInstance();

        //when
        boolean hasActiveSubjects = processInstance.hasActiveSubjects();

        //then
        assertThat(hasActiveSubjects, is(false));
    }

    @Test
    public void testHaveActiveSubjects_withProcessSubject() {
        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("name", Arrays.asList(new Role("name")));

        ProcessInstance processInstance = new ProcessInstance();
        UserSubject userSubject = new UserSubject(processInstance, userSubjectModel, null);
        processInstance.addSubject(userSubject);

        //when
        boolean hasActiveSubjects = processInstance.hasActiveSubjects();

        //then
        assertThat(hasActiveSubjects, is(false));
    }

    @Test
    public void testHaveActiveSubjects_withActiveSubject() {
        //given
        ProcessInstance processInstance = new ProcessInstance();
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));

        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("name", Arrays.asList(new Role("name")));
        UserSubject subject = new UserSubject(processInstance, userSubjectModel, null);
        subject.setCurrentState(new FunctionState("start"));
        assertTrue(subject.isActive());

        processInstance.addSubject(subject);

        //when
        boolean hasActiveSubjects = processInstance.hasActiveSubjects();

        //then
        assertThat(hasActiveSubjects, is(true));
    }

    @Test
    public void testFindActiveSubject_withMultipleSubjects() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Duplicate key");

        //given
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));
        UserSubjectModel subjectModel = MockData.spyUserSubjectModel(1l, processModel, "name", new Role("name"));

        FunctionState startState = new FunctionState("start");
        startState.setEventType(StateEventType.START);

        FunctionState endState = new FunctionState("end");
        endState.setEventType(StateEventType.END);
        startState.addHead(endState);

        when(subjectModel.getStates()).thenReturn(Arrays.asList(startState, endState));

        ProcessInstance processInstance = new ProcessInstance();

        UserSubject subject1 = new UserSubject(processInstance, subjectModel, new User("username"));
        subject1.setCurrentState(startState);
        assertThat(processInstance.getSubjects(), hasItem(subject1));

        UserSubject subject2 = new UserSubject(processInstance, subjectModel, new User("username"));
        subject2.setCurrentState(startState);
        assertThat(processInstance.getSubjects(), hasItem(subject2));

        //when
        Optional<Subject> result = processInstance.findActiveSubject(subjectModel);

        //then
        fail("findActiveSubject with multiple active subjects must throw Exception, but was " + result);
    }

    @Test
    public void testFindActiveSubject() {
        //given
        SubjectModel subjectModel = null;
        ProcessInstance instance = new ProcessInstance();

        //then
        Optional<Subject> result = instance.findActiveSubject(subjectModel);

        //then
        assertFalse(result.isPresent());
    }

}
