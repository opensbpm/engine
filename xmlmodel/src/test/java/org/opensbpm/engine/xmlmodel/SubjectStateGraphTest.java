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
package org.opensbpm.engine.xmlmodel;

import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import org.opensbpm.engine.api.model.definition.SubjectStateGraph;
import java.util.List;
import junit.framework.AssertionFailedError;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.oneOf;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.opensbpm.engine.examples.ExampleModels;

@RunWith(Theories.class)
public class SubjectStateGraphTest {

    private ProcessDefinition processDefinition;

    @Before
    public void setUp() throws Exception {
        processDefinition = new ProcessModel().unmarshal(ExampleModels.getBookPage103());
    }

    private SubjectDefinition findSubject(String subjectName) {
        return processDefinition.getSubjects().stream()
                .filter(subject -> subject.getName().equals(subjectName))
                .findFirst().orElseThrow(() -> new AssertionFailedError());
    }

    private void assertStates(List<StateDefinition> stateDefinitions, String... stateNames) {
        for (StateDefinition stateDefinition : stateDefinitions) {
            assertThat(stateDefinition.getName(), oneOf(stateNames));
        }
        assertThat(stateDefinitions, hasSize(stateNames.length));
    }
    @DataPoints("roots")
    public static SubjectStates[] ROOTSTATES = new SubjectStates[]{
        new SubjectStates("Mitarbeiter", "DR-Antrag ausfüllen"),
        new SubjectStates("Vorgesetzter", "DR-Antrag empfangen"),
        new SubjectStates("Reisestelle", "DR-Antrag empfangen")
    };

    @Test
    @Theory
    public void testGetRoots(@FromDataPoints("roots") SubjectStates subjectStates) {
        //given
        SubjectDefinition subjectDefinition = findSubject(subjectStates.subject);
        SubjectStateGraph instance = new SubjectStateGraph(subjectDefinition);

        //when
        List<StateDefinition> result = instance.getRoots();

        //then
        assertStates(result, subjectStates.states);
    }

    @DataPoints("leafs")
    public static SubjectStates[] LEAFSTATES = new SubjectStates[]{
        new SubjectStates("Mitarbeiter", "DR beendet", "Abgelehnt"),
        //TODO fix duplicate "Ende" states
        new SubjectStates("Vorgesetzter", "Ende", "Ende"),
        new SubjectStates("Reisestelle", "Reise gebucht")
    };

    @Test
    @Theory
    public void testGetLeafs(@FromDataPoints("leafs") SubjectStates subjectStates) {
        //given
        SubjectDefinition subjectDefinition = findSubject(subjectStates.subject);
        SubjectStateGraph instance = new SubjectStateGraph(subjectDefinition);

        //when
        List<StateDefinition> result = instance.getLeafs();

        //then
        assertStates(result, subjectStates.states);
    }

    public static class SubjectStates {

        private final String subject;
        private final String[] states;

        public SubjectStates(String subject, String... states) {
            this.subject = subject;
            this.states = states;
        }

        @Override
        public String toString() {
            return "SubjectStates{" + "subject=" + subject + ", states=" + states + '}';
        }

    }
}
