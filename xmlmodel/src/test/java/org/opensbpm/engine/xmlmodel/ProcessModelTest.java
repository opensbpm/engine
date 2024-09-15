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
package org.opensbpm.engine.xmlmodel;

import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.containsHeads;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.containsPermisssions;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isFieldPermission;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isFunctionState;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isPermission;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isNestedPermission;
import static org.opensbpm.engine.api.junit.ModelUtils.getState;
import static org.opensbpm.engine.api.junit.ModelUtils.getSubject;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isField;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isReference;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObject;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isStarterSubjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isSubjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isToMany;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isToOne;
import static org.opensbpm.engine.api.junit.ReceiveStateDefinitionMatchers.isMessage;
import static org.opensbpm.engine.api.junit.ReceiveStateDefinitionMatchers.isReceiveState;
import static org.opensbpm.engine.api.junit.SendStateDefinitionMatchers.isSendState;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import jakarta.xml.bind.JAXBException;
import static net.bytebuddy.implementation.FixedValue.nullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensbpm.engine.examples.ExampleModels;

public class ProcessModelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConvert() throws Exception {
        //given
        InputStream inputStream = ExampleModels.getDienstreiseantrag();
        ProcessDefinition processDefinition = new ProcessModel().unmarshal(inputStream);
        
        StringWriter stringWriter = new StringWriter();
        new ProcessModel().marshal(processDefinition, stringWriter);

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(new StringReader(stringWriter.toString()));

        //then
        assertThat(result, is(not(nullValue())));
        assertThat(result.getName(), is("Erweiterter Dienstreiseantrag"));
        assertThat(result.getVersion(), is(1));
        assertThat(result.getState(), is(ProcessModelState.ACTIVE));
        assertThat(result.getDescription(), not(is(emptyString())));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Mitarbeiter"),
                        isSubjectName("Vorgesetzter"),
                        isSubjectName("Reisestelle")
                ));

        assertThat("wrong states for 'Mitarbeiter' ", getSubject(result,"Mitarbeiter").getStates(),
                containsInAnyOrder(isFunctionState("DR-Antrag ausfüllen",
                        containsPermisssions(isPermission("DR-Antrag",
                                isFieldPermission("Name", Permission.WRITE, true),
                                isFieldPermission("Reisebeginn", Permission.WRITE, true),
                                isFieldPermission("Reiseende", Permission.WRITE, true),
                                isFieldPermission("Reiseziel", Permission.WRITE, true),
                                isNestedPermission("Mitreisende",
                                        isFieldPermission("Antragsteller", Permission.WRITE, true),
                                        isFieldPermission("Bemerkung", Permission.WRITE, true)
                                )
                        )
                        ),
                        containsHeads(isSendState("DR-Antrag an Vorgesetzer senden"))
                ),
                        isSendState("DR-Antrag an Vorgesetzer senden", "Vorgesetzter", "DR-Antrag", "Antwort von Vorgesetzter empfangen"),
                        isReceiveState("Antwort von Vorgesetzter empfangen", isMessage("Genehmigung", "Buchung von Reisestelle empfangen"), isMessage("Ablehnung", "DR-Antrag zurückziehen/ändern")),
                        isReceiveState("Buchung von Reisestelle empfangen", isMessage("Buchung", "DR antreten")),
                        isFunctionState("DR antreten"),
                        isFunctionState("DR beendet"),
                        isFunctionState("DR-Antrag zurückziehen/ändern",
                                containsHeads(isSendState("DR-Antrag an Vorgesetzer senden"),
                                        isFunctionState("DR-Antrag zurückziehen")
                                )
                        ),
                        isFunctionState("DR-Antrag zurückziehen")
                ));

        assertThat("wrong states for 'Vorgesetzter' ", getSubject(result,"Vorgesetzter").getStates(),
                containsInAnyOrder(isReceiveState("DR-Antrag empfangen", isMessage("DR-Antrag", "DR-Antrag prüfen")),
                        isFunctionState("DR-Antrag prüfen",
                                containsPermisssions(isPermission("DR-Antrag",
                                        isFieldPermission("Name", Permission.READ, false),
                                        isFieldPermission("Reisebeginn", Permission.WRITE, true),
                                        isFieldPermission("Reiseende", Permission.WRITE, true),
                                        isFieldPermission("Reiseziel", Permission.READ, false),
                                        isNestedPermission("Mitreisende",
                                                isFieldPermission("Antragsteller", Permission.READ, false),
                                                isFieldPermission("Bemerkung", Permission.READ, false)
                                        ),
                                        isNestedPermission("Kostenstelle",
                                                isFieldPermission("Nummer", Permission.WRITE, true),
                                                isFieldPermission("Faktor", Permission.WRITE, true)
                                        )
                                ),
                                        isPermission("Genehmigung", "Bemerkung", Permission.WRITE, false),
                                        isPermission("Ablehnung", "Begründung", Permission.WRITE, false)
                                ),
                                containsHeads(
                                        isSendState("Genehmigen"),
                                        isSendState("Ablehnen")
                                )),
                        isSendState("Genehmigen", "Mitarbeiter", "Genehmigung", "Buchung veranlassen"),
                        isSendState("Buchung veranlassen", "Reisestelle", "genehmigter DR-Antrag"),
                        isSendState("Ablehnen", "Mitarbeiter", "Ablehnung")
                ));

        assertThat("wrong states for 'Reisestelle' ", getSubject(result,"Reisestelle").getStates(),
                containsInAnyOrder(isReceiveState("DR-Antrag empfangen", isMessage("genehmigter DR-Antrag", "Buchen")),
                        isFunctionState("Buchen",
                                containsPermisssions(isPermission("Buchung",
                                        isFieldPermission("Hotel", Permission.WRITE, true)
                                )),
                                containsHeads(isSendState("Reise gebucht"))
                        ),
                        isSendState("Reise gebucht", "Mitarbeiter", "Buchung")
                ));

        assertThat("wrong objects", result.getObjects(),
                containsInAnyOrder(
                        isObject("DR-Antrag",
                                "${Name} - ${Reiseziel}: ${Reisebeginn} - ${Reiseende}",
                                isField("Name", FieldType.STRING),
                                isField("Reisebeginn", FieldType.DATE),
                                isField("Reiseende", FieldType.DATE),
                                isField("Reiseziel", FieldType.STRING),
                                isToMany("Mitreisende",
                                        isReference("Antragsteller", "Angestellter"),
                                        isField("Bemerkung", FieldType.STRING)
                                ),
                                isToOne("Kostenstelle",
                                        isField("Nummer", FieldType.NUMBER),
                                        isField("Faktor", FieldType.DECIMAL)
                                )
                        ),
                        isObject("Angestellter",
                                isField("Nummer", FieldType.NUMBER),
                                isField("Name", FieldType.STRING)
                        ),
                        isObject("Genehmigung",
                                isField("Bemerkung", FieldType.STRING)),
                        isObject("Ablehnung",
                                isField("Begründung", FieldType.STRING)),
                        isObject("Buchung",
                                isField("Hotel", FieldType.STRING)),
                        isObjectName("genehmigter DR-Antrag")
                ));

    }

    @Test
    public void testAllElements() throws Exception {
        //given
        InputStream inputStream = getClass().getResourceAsStream("allElements.xml");
        ProcessDefinition processDefinition = new ProcessModel().unmarshal(inputStream);
        
        StringWriter stringWriter = new StringWriter();
        new ProcessModel().marshal(processDefinition, stringWriter);

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(new StringReader(stringWriter.toString()));

        //then
        assertThat(result, is(not(nullValue())));
        assertThat(result.getName(), is("All Elements"));
        assertThat(result.getVersion(), is(1));
        //TODO assertThat(result.getState(), is(ProcessModelState.INACTIVE));
        assertThat(result.getDescription(), not(is(emptyString())));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Starter Subject")
                ));

        assertThat("wrong states for 'Subject' ", getSubject(result, "Starter Subject").getStates(),
                containsInAnyOrder(
                        isFunctionState("Start State",
                            containsHeads(isFunctionState("End State"))
                        ),
                        isFunctionState("End State")
                ));

        assertThat("wrong objects", result.getObjects(),
                containsInAnyOrder(
                        isObject("My Object", 
                                isField("Name", FieldType.STRING),
                                isReference("Referencing Object","Referenced Object"),
                                isToOne("Single Nested",
                                        isField("Name", FieldType.STRING)
                                ),
                                isToMany("Many Nested",
                                        isField("Name", FieldType.STRING)
                                )
                        ),
                        isObject("Referenced Object",
                                isField("Name", FieldType.STRING))
                ));

    }

    @Test
    public void testFunctionparameters() throws Exception {
        //given
        InputStream inputStream = ExampleModels.getRechungslegung();
        ProcessDefinition processDefinition = new ProcessModel().unmarshal(inputStream);
        
        StringWriter stringWriter = new StringWriter();
        new ProcessModel().marshal(processDefinition, stringWriter);

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(new StringReader(stringWriter.toString()));

        //then
        assertThat(result.getName(), is("Rechnungslegung"));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Mitarbeiter"),
                        isSubjectName("Druckservice")
                ));
        
        FunctionStateDefinition createPrintFunction = 
                (FunctionStateDefinition)getState(result, "Druckservice", "Druck erstellen");

        assertThat("wrong State for 'Druckservice' 'Druck erstellen'",
                createPrintFunction.getParameters(), hasEntry("layout","Rechnung")
        );

    }

    @Ignore
    @Test
    public void deserializeWithMissingToState() throws JAXBException {
        thrown.expectMessage(startsWith("No state for toState"));

        //given
        InputStream inputStream = getClass().getResourceAsStream("/org/opensbpm/engine/xmlmodel/missingToState.xml");

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(inputStream);

        //then
        fail("unmarshal of invalid xml must fail but was " + result);
    }

    @Ignore
    @Test
    public void deserializeWithMissingNessageObject() throws JAXBException {
        thrown.expectMessage(startsWith("No object for object-name"));

        //given
        InputStream inputStream = getClass().getResourceAsStream("/org/opensbpm/engine/xmlmodel/missingMessageObject.xml");

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(inputStream);

        //then
        fail("unmarshal of invalid xml must fail but was " + result);
    }

}
