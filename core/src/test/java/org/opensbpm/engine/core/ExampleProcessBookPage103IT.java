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

import java.time.LocalDate;
import java.time.Month;
import org.junit.Before;
import org.junit.Test;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
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
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.hasOneState;

public class ExampleProcessBookPage103IT extends WorkflowTestCase {

    private ProcessModelInfo modelInfo;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ProcessDefinition processDefinition = new ProcessModel().unmarshal(ExampleModels.getBookPage103());
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

        //Mitarbeiter: "DR-Antrag an Vorgesetzter senden" (send message)
        task = employee.getTask("DR-Antrag ausfüllen");
        task.setValue("DR-Antrag", "Name", "Test");
        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 1));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 10));
        task.setValue("DR-Antrag", "Reiseziel", "Test");
        employee.execute(task, "DR-Antrag an Vorgesetzter senden");
        //synchronized Message, send-state must be current state for user employee
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag an Vorgesetzter senden", StateFunctionType.SEND),
                isSubjectState("Vorgesetzter", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(hasOneState("DR-Antrag prüfen"));
        travelagency.assertTasks(is(empty()));
        //

        //Vorgesetzter: "DR-Antrag prüfen", (task after receive message), read message from Mitarbeiter, send message to Mitarbeiter
        task = superior.getTask("DR-Antrag prüfen");
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 1)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 10)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test"));
        superior.execute(task, "Ablehnen");
        processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                isSubjectState("Mitarbeiter", "Abgelehnt", StateFunctionType.FUNCTION),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //dumpEngineEvents();
        processController.assertTrail(contains(
                isTrail("Mitarbeiter", "DR-Antrag ausfüllen"),
                isTrail("Mitarbeiter", "DR-Antrag an Vorgesetzter senden"),
                isTrail("Vorgesetzter", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "DR-Antrag prüfen"),
                isTrail("Mitarbeiter", "Antwort von Vorgesetzter empfangen"),
                isTrail("Vorgesetzter", "Ablehnen"),
                isTrail("Mitarbeiter", "Abgelehnt"),
                isTrail("Vorgesetzter", "Ende")
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

        //Mitarbeiter: "DR-Antrag an Vorgesetzter senden" (send message)
        task = employee.getTask("DR-Antrag ausfüllen");
        task.setValue("DR-Antrag", "Name", "Test");
        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 1));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 10));
        task.setValue("DR-Antrag", "Reiseziel", "Test");
        employee.execute(task, "DR-Antrag an Vorgesetzter senden");
        //synchronized Message, send-state must be current state for user employee
        assertEngineEvents(
                isUserTaskChangedEvent(employee.getId(), "DR-Antrag ausfüllen", Type.DELETE),
                isUserTaskChangedEvent(superior.getId(), "DR-Antrag prüfen", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag an Vorgesetzter senden", StateFunctionType.SEND),
                isSubjectState("Vorgesetzter", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(is(empty()));
        superior.assertTasks(hasOneState("DR-Antrag prüfen"));

        //
        //Vorgsetzter: "DR-Antrag prüfen", (task after receive message), read message from Mitarbeiter, send message to Mitarbeiter
        task = superior.getTask("DR-Antrag prüfen");
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 1)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 10)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test"));

        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 2));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 9));
        superior.execute(task, "Genehmigen");
        assertEngineEvents(
                isUserTaskChangedEvent(superior.getId(), "DR-Antrag prüfen", Type.DELETE),
                isUserTaskChangedEvent(employee.getId(), "DR antreten", Type.CREATE),
                isUserTaskChangedEvent(travelagency.getId(), "Buchen", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION),
                isSubjectState("Reisestelle", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(hasOneState("DR antreten"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(hasOneState("Buchen"));
        //

        //Reisestelle: "Buchen", (task after receive message), read message from Vorgesetzter
        task = travelagency.getTask("Buchen");
        travelagency.execute(task, "Reise gebucht");
        assertEngineEvents(
                isUserTaskChangedEvent(travelagency.getId(), "Buchen", Type.DELETE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION),
                isSubjectState("Reisestelle", "Reise gebucht", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(hasOneState("DR antreten"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Mitarbeiter: Task "DR antreten" 
        task = employee.getTask("DR antreten");
        employee.execute(task, "DR beendet");
        assertEngineEvents(
                isUserTaskChangedEvent(employee.getId(), "DR antreten", Type.DELETE),
                isProcessInstanceChangedEvent(Type.UPDATE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(employee.getId(), "DR beendet", Type.DELETE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(superior.getId(), "Ende", Type.DELETE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(travelagency.getId(), "Reise gebucht", Type.DELETE)
        );
        processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                isSubjectState("Mitarbeiter", "DR beendet", StateFunctionType.FUNCTION),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION),
                isSubjectState("Reisestelle", "Reise gebucht", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        processController.assertTrail(contains(
                isTrail("Mitarbeiter", "DR-Antrag ausfüllen"),
                isTrail("Mitarbeiter", "DR-Antrag an Vorgesetzter senden"),
                isTrail("Vorgesetzter", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "DR-Antrag prüfen"),
                isTrail("Mitarbeiter", "Antwort von Vorgesetzter empfangen"),
                isTrail("Vorgesetzter", "Genehmigen"),
                isTrail("Vorgesetzter", "Buchung veranlassen"),
                isTrail("Reisestelle", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "Ende"),
                isTrail("Reisestelle", "Buchen"),
                isTrail("Reisestelle", "Reise gebucht"),
                isTrail("Mitarbeiter", "DR antreten"),
                isTrail("Mitarbeiter", "DR beendet")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

    @Test
    public void testApproveBookLate() throws Exception {
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

        //Mitarbeiter: "DR-Antrag an Vorgesetzter senden" (send message)
        task = employee.getTask("DR-Antrag ausfüllen");
        task.setValue("DR-Antrag", "Name", "Test");
        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 1));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 10));
        task.setValue("DR-Antrag", "Reiseziel", "Test");
        employee.execute(task, "DR-Antrag an Vorgesetzter senden");
        //synchronized Message, send-state must be current state for user employee
        assertEngineEvents(
                isUserTaskChangedEvent(employee.getId(), "DR-Antrag ausfüllen", Type.DELETE),
                isUserTaskChangedEvent(superior.getId(), "DR-Antrag prüfen", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR-Antrag an Vorgesetzter senden", StateFunctionType.SEND),
                isSubjectState("Vorgesetzter", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(is(empty()));
        superior.assertTasks(hasOneState("DR-Antrag prüfen"));

        //
        //Vorgsetzter: "DR-Antrag prüfen", (task after receive message), read message from Mitarbeiter, send message to Mitarbeiter
        task = superior.getTask("DR-Antrag prüfen");
        assertThat(task.getValue("DR-Antrag", "Name"), is("Test"));
        assertThat(task.getValue("DR-Antrag", "Reisebeginn"), is(LocalDate.of(2018, Month.SEPTEMBER, 1)));
        assertThat(task.getValue("DR-Antrag", "Reiseende"), is(LocalDate.of(2018, Month.SEPTEMBER, 10)));
        assertThat(task.getValue("DR-Antrag", "Reiseziel"), is("Test"));

        task.setValue("DR-Antrag", "Reisebeginn", LocalDate.of(2018, Month.SEPTEMBER, 2));
        task.setValue("DR-Antrag", "Reiseende", LocalDate.of(2018, Month.SEPTEMBER, 9));
        superior.execute(task, "Genehmigen");
        assertEngineEvents(
                isUserTaskChangedEvent(superior.getId(), "DR-Antrag prüfen", Type.DELETE),
                isUserTaskChangedEvent(employee.getId(), "DR antreten", Type.CREATE),
                isUserTaskChangedEvent(travelagency.getId(), "Buchen", Type.CREATE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Antwort von Vorgesetzter empfangen", StateFunctionType.RECEIVE),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION),
                isSubjectState("Reisestelle", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(hasOneState("DR antreten"));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(hasOneState("Buchen"));
        //

        //Mitarbeiter: Task "DR antreten" 
        task = employee.getTask("DR antreten");
        employee.execute(task, "DR beendet");
        assertEngineEvents(
                isUserTaskChangedEvent(employee.getId(), "DR antreten", Type.DELETE)
        );
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "DR beendet", StateFunctionType.FUNCTION),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION),
                isSubjectState("Reisestelle", "DR-Antrag empfangen", StateFunctionType.RECEIVE)
        ));
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(hasOneState("Buchen"));

        //Reisestelle: Task "Buchen" 
        task = travelagency.getTask("Buchen");
        travelagency.execute(task, "Reise gebucht");
        assertEngineEvents(
                isUserTaskChangedEvent(travelagency.getId(), "Buchen", Type.DELETE),
                isProcessInstanceChangedEvent(Type.UPDATE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(employee.getId(), "DR beendet", Type.DELETE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(superior.getId(), "Ende", Type.DELETE),
                isUserProcessInstanceChangedEvent(Type.DELETE),
                isUserTaskChangedEvent(travelagency.getId(), "Reise gebucht", Type.DELETE)
        );
        processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                isSubjectState("Mitarbeiter", "DR beendet", StateFunctionType.FUNCTION),
                isSubjectState("Vorgesetzter", "Ende", StateFunctionType.FUNCTION),
                isSubjectState("Reisestelle", "Reise gebucht", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(is(empty()));
        superior.assertTasks(is(empty()));
        travelagency.assertTasks(is(empty()));
        //

        //Reisestelle: "Buchen", (task after receive message), 'Reisestelle' is never active, 
        //'DR beendet' is executed bevor
        processController.assertTrail(contains(
                isTrail("Mitarbeiter", "DR-Antrag ausfüllen"),
                isTrail("Mitarbeiter", "DR-Antrag an Vorgesetzter senden"),
                isTrail("Vorgesetzter", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "DR-Antrag prüfen"),
                isTrail("Mitarbeiter", "Antwort von Vorgesetzter empfangen"),
                isTrail("Vorgesetzter", "Genehmigen"),
                isTrail("Vorgesetzter", "Buchung veranlassen"),
                isTrail("Reisestelle", "DR-Antrag empfangen"),
                isTrail("Vorgesetzter", "Ende"),
                isTrail("Mitarbeiter", "DR antreten"),
                isTrail("Mitarbeiter", "DR beendet"),
                isTrail("Reisestelle", "Buchen"),
                isTrail("Reisestelle", "Reise gebucht")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

}
