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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.model.ObjectReference;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.junit.TestTask;
import org.opensbpm.engine.core.junit.UserProcessController;
import org.opensbpm.engine.core.junit.UserProcessController.ProcessInstanceController;
import org.opensbpm.engine.core.junit.WorkflowTestCase;
import org.opensbpm.engine.examples.ExampleModels;
import org.opensbpm.engine.xmlmodel.ProcessModel;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.opensbpm.engine.api.junit.AuditTrailMatchers.isTrail;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isProcessInstanceChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserProcessInstanceChangedEvent;
import static org.opensbpm.engine.api.junit.EngineEventMatcher.isUserTaskChangedEvent;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.hasSubjects;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isState;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isSubjectState;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isUserSubjectState;
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.hasOneState;
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.isNextState;

public class ExampleProcessDienstreiseantragIT extends WorkflowTestCase {

    private ProcessModelInfo modelInfo;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ProcessDefinition processDefinition = new ProcessModel().unmarshal(ExampleModels.getDienstreiseantrag());
        modelInfo = doInTransaction(()
                -> modelService.save(processDefinition)
        );
    }

    @Test
    public void testReject() throws Exception {
        UserProcessController employee = createUserController("Mitarbeiter", "Angestellte");
        UserProcessController superior = createUserController("Vorgesetzter", "Abteilungsleiter");
        UserProcessController travelagency = createUserController("Reisestelle", "Reisestelle");

        ProcessInstanceController processController = employee.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag ausfüllen", StateFunctionType.FUNCTION)
        ));

        TestTask task;

        //
        employee.assertTasks(hasOneState("DR-Antrag ausfüllen"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Mitarbeiter: "DR-Antrag an Vorgesetzer senden" (send message)
        task = employee.getTask("DR-Antrag ausfüllen");
        task.assertNextStates(
                isNextState("DR-Antrag an Vorgesetzer senden", true)
        );
        task.setValue("DR-Antrag", "Name", "Test");
        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 1));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 10));
        task.setValue("DR-Antrag", "Reiseziel", "Test");
        employee.execute(task, "DR-Antrag an Vorgesetzer senden");
        //synchronized Message, send-state must be current state for user employee
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag an Vorgesetzer senden", StateFunctionType.SEND),
                isSubjectState("Vorgesetzter", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(hasOneState("DR-Antrag prüfen"));
        travelagency.assertTasks(is(empty()));
        //

        //Vorgesetzter: "DR-Antrag prüfen", (task after receive message), read message from Mitarbeiter, send message to Mitarbeiter
        task = superior.getTask("DR-Antrag prüfen");
        task.assertNextStates(
                isNextState("Genehmigen", true),
                isNextState("Ablehnen", true)
        );
        assertThat(task.getDisplayName("DR-Antrag"), is("Test - Test: 2018-09-01 - 2018-09-10"));
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 1)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 10)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test"));

        task.setValue("DR-Antrag", "Kostenstelle.Nummer", 1);
        task.setValue("DR-Antrag", "Kostenstelle.Faktor", BigDecimal.ONE);
        superior.execute(task, "Ablehnen");
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isSubjectState("Vorgesetzter", "Ablehnen", StateFunctionType.SEND)
        ));

        //
        employee.assertTasks(hasOneState("DR-Antrag zurückziehen/ändern"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Mitarbeiter: "DR-Antrag zurückziehen/ändern", (task after receive message), read message from Vorgesetzter,send message
        task = employee.getTask("DR-Antrag zurückziehen/ändern");
        task.assertNextStates(
                isNextState("DR-Antrag an Vorgesetzer senden", true),
                isNextState("DR-Antrag zurückziehen", true)
        );
        assertThat(task.getDisplayName("DR-Antrag"), is("Test - Test: 2018-09-01 - 2018-09-10"));
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 2));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 9));
        task.setValue("DR-Antrag", "Reiseziel", "Test-Changed");
        employee.execute(task, "DR-Antrag an Vorgesetzer senden");
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag an Vorgesetzer senden", StateFunctionType.SEND),
                isUserSubjectState(superior.getUsername(), "Vorgesetzter", "Ablehnen", StateFunctionType.SEND)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(hasOneState("DR-Antrag prüfen"));
        travelagency.assertTasks(is(empty()));

        //Vorgesetzter: "DR-Antrag prüfen", (task after receive message), read message from Mitarbeiter, send message to Mitarbeiter
        task = superior.getTask("DR-Antrag prüfen");
        task.assertNextStates(
                isNextState("Genehmigen", true),
                isNextState("Ablehnen", true)
        );
        assertThat(task.getDisplayName("DR-Antrag"), is("Test - Test-Changed: 2018-09-02 - 2018-09-09"));
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 2)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 9)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test-Changed"));
        superior.execute(task, "Ablehnen");
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isUserSubjectState(superior.getUsername(), "Vorgesetzter", "Ablehnen", StateFunctionType.SEND),
                isUserSubjectState(superior.getUsername(), "Vorgesetzter", "Ablehnen", StateFunctionType.SEND)
        ));

        //
        employee.assertTasks(hasOneState("DR-Antrag zurückziehen/ändern"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Mitarbeiter: "DR-Antrag zurückziehen/ändern", (task after receive message), read message from Vorgesetzter
        task = employee.getTask("DR-Antrag zurückziehen/ändern");
        task.assertNextStates(
                isNextState("DR-Antrag an Vorgesetzer senden", true),
                isNextState("DR-Antrag zurückziehen", true)
        );
        employee.execute(task, "DR-Antrag zurückziehen");
        processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag zurückziehen", StateFunctionType.FUNCTION),
                isUserSubjectState(superior.getUsername(), "Vorgesetzter", "Ablehnen", StateFunctionType.SEND),
                isUserSubjectState(superior.getUsername(), "Vorgesetzter", "Ablehnen", StateFunctionType.SEND)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //dumpEngineEvents();
        processController.assertTrail(contains(
                isTrail("Mitarbeiter", "DR-Antrag ausfüllen"),
                isTrail("Mitarbeiter", "DR-Antrag an Vorgesetzer senden"),
                isTrail("Vorgesetzter", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "DR-Antrag prüfen"),
                isTrail("Mitarbeiter", "Antwort von Vorgesetzter empfangen"),
                isTrail("Vorgesetzter", "Ablehnen"),
                isTrail("Mitarbeiter", "DR-Antrag zurückziehen/ändern"),
                isTrail("Mitarbeiter", "DR-Antrag an Vorgesetzer senden"),
                isTrail("Vorgesetzter", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "DR-Antrag prüfen"),
                isTrail("Mitarbeiter", "Antwort von Vorgesetzter empfangen"),
                isTrail("Vorgesetzter", "Ablehnen"),
                isTrail("Mitarbeiter", "DR-Antrag zurückziehen/ändern"),
                isTrail("Mitarbeiter", "DR-Antrag zurückziehen")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    @Test
    public void testApprove() throws Exception {
        UserProcessController employee = createUserController("Mitarbeiter", "Angestellte");
        UserProcessController superior = createUserController("Vorgesetzter", "Abteilungsleiter");
        UserProcessController travelagency = createUserController("Reisestelle", "Reisestelle");

        engineEventsCollector.clear();

        ProcessInstanceController processController = employee.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag ausfüllen", StateFunctionType.FUNCTION)
        ));

        TestTask task;

        //after start
        assertEngineEvents(
                isUserTaskChangedEvent(employee.getId(), "DR-Antrag ausfüllen", Type.CREATE),
                isProcessInstanceChangedEvent(Type.CREATE)
        );
        employee.assertTasks(hasOneState("DR-Antrag ausfüllen"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Mitarbeiter: "DR-Antrag an Vorgesetzer senden" (send message)
        task = employee.getTask("DR-Antrag ausfüllen");
        task.assertNextStates(
                isNextState("DR-Antrag an Vorgesetzer senden", true)
        );
        task.setValue("DR-Antrag", "Name", "Test");
        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 1));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 10));
        task.setValue("DR-Antrag", "Reiseziel", "Test");
        task.setValue("DR-Antrag", "Mitreisende[0].Antragsteller", ObjectReference.of("1", "Test").toMap());
        task.setValue("DR-Antrag", "Mitreisende[0].Bemerkung", "Test 1");
        task.setValue("DR-Antrag", "Mitreisende[1].Antragsteller",ObjectReference.of("1", "Test").toMap());
        task.setValue("DR-Antrag", "Mitreisende[1].Bemerkung", "Test 2");
        employee.execute(task, "DR-Antrag an Vorgesetzer senden");
        //synchronized Message, send-state must be current state for user employee
        assertEngineEvents(
                isUserTaskChangedEvent(employee.getId(), "DR-Antrag ausfüllen", Type.DELETE),
                isUserTaskChangedEvent(superior.getId(), "DR-Antrag prüfen", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag an Vorgesetzer senden", StateFunctionType.SEND),
                isSubjectState("Vorgesetzter", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(is(empty()));
        superior.assertTasks(hasOneState("DR-Antrag prüfen"));

        //
        //Vorgsetzter: "DR-Antrag prüfen", (task after receive message), read message from Mitarbeiter, send message to Mitarbeiter
        task = superior.getTask("DR-Antrag prüfen");
        task.assertNextStates(
                isNextState("Genehmigen", true),
                isNextState("Ablehnen", true)
        );
        assertThat(task.getDisplayName("DR-Antrag"), is("Test - Test: 2018-09-01 - 2018-09-10"));
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 1)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 10)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Mitreisende[0].Antragsteller"), is(ObjectReference.of("1", "Test").toMap()));
        assertThat(task.getValue("DR-Antrag", "Mitreisende[0].Bemerkung"), is("Test 1"));
        assertThat(task.getValue("DR-Antrag", "Mitreisende[1].Antragsteller"), is(ObjectReference.of("1", "Test").toMap()));
        assertThat(task.getValue("DR-Antrag", "Mitreisende[1].Bemerkung"), is("Test 2"));

        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 2));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 9));
        task.setValue("DR-Antrag", "Kostenstelle.Nummer", 1);
        task.setValue("DR-Antrag", "Kostenstelle.Faktor", BigDecimal.ONE);
        task.setValue("Genehmigung", "Bemerkung", "Genehmigt");
        superior.execute(task, "Genehmigen");
        assertEngineEvents(
                isUserTaskChangedEvent(superior.getId(), "DR-Antrag prüfen", Type.DELETE),
                isUserTaskChangedEvent(travelagency.getId(), "Buchen", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isSubjectState("Vorgesetzter", "Buchung veranlassen", StateFunctionType.SEND),
                isSubjectState("Reisestelle", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(hasOneState("Buchen"));
        //

        //Reisestelle: "Buchen", (task after receive message), read message from Vorgesetzter
        task = travelagency.getTask("Buchen");
        task.assertNextStates(
                isNextState("Reise gebucht", true)
        );
        task.setValue("Buchung", "Hotel", "Test-Hotel");
        travelagency.execute(task, "Reise gebucht");
        assertEngineEvents(
                isUserTaskChangedEvent(travelagency.getId(), "Buchen", Type.DELETE),
                isUserTaskChangedEvent(employee.getId(), "DR antreten", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isSubjectState("Vorgesetzter", "Buchung veranlassen", StateFunctionType.SEND),
                isSubjectState("Reisestelle", "Reise gebucht", StateFunctionType.SEND)
        ));

        //
        employee.assertTasks(hasOneState("DR antreten"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Mitarbeiter: Task "DR antreten" 
        task = employee.getTask("DR antreten");
        task.assertNextStates(
                isNextState("DR beendet", true)
        );
        assertThat(task.getDisplayName("DR-Antrag"), is("Test - Test: 2018-09-02 - 2018-09-09"));
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 2)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 9)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test"));
        assertThat(task.getValue("Genehmigung", "Bemerkung"), is("Genehmigt"));
        assertThat(task.getValue("Buchung", "Hotel"), is("Test-Hotel"));
        employee.execute(task, "DR beendet");
        assertEngineEvents(
                isProcessInstanceChangedEvent(Type.UPDATE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(employee.getId(), "DR beendet", Type.DELETE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserProcessInstanceChangedEvent(Type.DELETE)
        );
        processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                isSubjectState("Mitarbeiter", "DR beendet", StateFunctionType.FUNCTION),
                isSubjectState("Vorgesetzter", "Buchung veranlassen", StateFunctionType.SEND),
                isSubjectState("Reisestelle", "Reise gebucht", StateFunctionType.SEND)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        processController.assertTrail(contains(
                isTrail("Mitarbeiter", "DR-Antrag ausfüllen"),
                isTrail("Mitarbeiter", "DR-Antrag an Vorgesetzer senden"),
                isTrail("Vorgesetzter", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "DR-Antrag prüfen"),
                isTrail("Mitarbeiter", "Antwort von Vorgesetzter empfangen"),
                isTrail("Vorgesetzter", "Genehmigen"),
                isTrail("Vorgesetzter", "Buchung veranlassen"),
                isTrail("Reisestelle", "DR-Antrag empfangen"),
                isTrail("Reisestelle", "Buchen"),
                isTrail("Reisestelle", "Reise gebucht"),
                isTrail("Mitarbeiter", "Buchung von Reisestelle empfangen"),
                //FIXME isTrail("Mitarbeiter", "DR antreten"),
                isTrail("Mitarbeiter", "DR beendet")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

}
