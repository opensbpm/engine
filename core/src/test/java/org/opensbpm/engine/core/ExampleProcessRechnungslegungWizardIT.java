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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.opensbpm.engine.api.junit.AuditTrailMatchers.isTrail;
import static org.opensbpm.engine.api.junit.CommonMatchers.isEmpty;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.hasSubjects;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isState;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isSubjectState;
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.hasOneState;
import static org.opensbpm.engine.api.junit.TaskInfoMatchers.isNextState;

public class ExampleProcessRechnungslegungWizardIT extends WorkflowTestCase {

    private ProcessModelInfo modelInfo;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ProcessDefinition processDefinition = new ProcessModel().unmarshal(ExampleModels.getRechungslegungWizard());
        modelInfo = doInTransaction(()
                -> modelService.save(processDefinition)
        );
    }

    @Test
    public void testCreate() throws Exception {
        UserProcessController employee = createUserController("Mitarbeiter", "Angestellte");

        ProcessInstanceController processController = employee.startProcess(modelInfo);
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Rechnung erfassen", StateFunctionType.FUNCTION)
        ));

        TestTask task;

        //
        employee.assertTasks(hasOneState("Rechnung erfassen"));
        //

        //"Rechnung erfassen"
        task = employee.getTask("Rechnung erfassen");
        task.assertNextStates(
                isNextState("Empfänger erfassen", false)
        );
        task.setValue("Rechnung", "Datum", LocalDate.of(2020, Month.AUGUST, 22));
        task.setValue("Rechnung", "Nummer", 1);
        employee.execute(task, "Empfänger erfassen");
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Empfänger erfassen", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(hasOneState("Empfänger erfassen"));
        //

        //"Empfänger erfassen"
        task = employee.getTask("Empfänger erfassen");
        task.assertNextStates(
                isNextState("Position erfassen", false)
        );
        task.setValue("Rechnung", "Empfänger.Name", "Test");
        task.setValue("Rechnung", "Empfänger.Adresse", "Test");
        employee.execute(task, "Position erfassen");
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Position erfassen", StateFunctionType.FUNCTION)
        ));
        //
        employee.assertTasks(hasOneState("Position erfassen"));
        //

        //"Position erfassen"
        task = employee.getTask("Position erfassen");
        task.assertNextStates(
                isNextState("Position erfassen", false),
                isNextState("Speichern", true)
        );
        task.setValue("Rechnung", "Position[0].Text", "Test");
        employee.execute(task, "Position erfassen");
        processController.assertState(ProcessInstanceState.ACTIVE, hasSubjects(
                isSubjectState("Mitarbeiter", "Position erfassen", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(hasOneState("Position erfassen"));
        //

        //"Position erfassen"
        task = employee.getTask("Position erfassen");
        task.assertNextStates(
                isNextState("Position erfassen", false),
                isNextState("Speichern", true)
        );
        task.setValue("Rechnung", "Position[1].Text", "Test");
        employee.execute(task, "Speichern");
        processController.assertState(ProcessInstanceState.FINISHED, hasSubjects(
                isSubjectState("Mitarbeiter", "Speichern", StateFunctionType.FUNCTION)
        ));

        //
        employee.assertTasks(isEmpty());
        //

        //dumpEngineEvents();
        processController.assertTrail(contains(
                isTrail("Mitarbeiter", "Rechnung erfassen"),
                isTrail("Mitarbeiter", "Empfänger erfassen"),
                isTrail("Mitarbeiter", "Position erfassen"),
                //FIXME isTrail("Mitarbeiter", "Position erfassen"),
                isTrail("Mitarbeiter", "Speichern")
        ));
        assertThat(processController.getProcessInfo(), isState(ProcessInstanceState.FINISHED));
    }

}
