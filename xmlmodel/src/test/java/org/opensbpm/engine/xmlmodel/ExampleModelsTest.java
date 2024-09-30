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

import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isSubjectName;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isFunctionState;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isPermission;
import static org.opensbpm.engine.api.junit.ReceiveStateDefinitionMatchers.isMessage;
import static org.opensbpm.engine.api.junit.ReceiveStateDefinitionMatchers.isReceiveState;
import static org.opensbpm.engine.api.junit.SendStateDefinitionMatchers.isSendState;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.containsHeads;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.containsPermisssions;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isStarterSubjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isField;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObject;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isFieldPermission;
import static org.opensbpm.engine.api.junit.ModelUtils.getPermission;
import static org.opensbpm.engine.api.junit.ModelUtils.getState;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isToMany;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isToOne;
import static org.opensbpm.engine.api.junit.ModelUtils.getSubject;
import org.opensbpm.engine.api.model.definition.PermissionDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import java.io.InputStream;
import org.opensbpm.engine.examples.ExampleModels;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasEntry;
import static org.opensbpm.engine.api.junit.FunctionStateDefinitionMatchers.isNestedPermission;

public class ExampleModelsTest {

    @Test
    public void testUnmarshalBookPage103() throws Exception {
        //given
        InputStream inputStream = ExampleModels.getBookPage103();

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(inputStream);

        //then
        assertThat(result.getName(), is("Dienstreiseantrag Seite/103"));
        assertThat(result.getVersion(), is(1));
        assertThat(result.getState(), is(ProcessModelState.ACTIVE));
        assertThat(result.getDescription(), not(emptyString()));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Mitarbeiter"),
                        isSubjectName("Vorgesetzter"),
                        isSubjectName("Reisestelle")
                ));

        assertThat("wrong states for 'Mitarbeiter' ", getSubject(result, "Mitarbeiter").getStates(),
                containsInAnyOrder(isFunctionState("DR-Antrag ausfüllen",
                        containsPermisssions(isPermission("DR-Antrag",
                                isFieldPermission("Name", Permission.WRITE, true),
                                isFieldPermission("Reisebeginn", Permission.WRITE, true),
                                isFieldPermission("Reiseende", Permission.WRITE, true),
                                isFieldPermission("Reiseziel", Permission.WRITE, true)
                        )
                        ),
                        containsHeads(isSendState("DR-Antrag an Vorgesetzer senden"))
                ),
                        isSendState("DR-Antrag an Vorgesetzer senden", "Vorgesetzter", "DR-Antrag", "Antwort von Vorgesetzter empfangen"),
                        isReceiveState("Antwort von Vorgesetzter empfangen", isMessage("Genehmigung", "DR antreten"), isMessage("Ablehnung", "Abgelehnt")),
                        isFunctionState("DR antreten", containsHeads(isFunctionState("DR beendet"))),
                        isFunctionState("DR beendet"),
                        isFunctionState("Abgelehnt")
                ));

        assertThat("wrong states for 'Vorgesetzter' ", getSubject(result, "Vorgesetzter").getStates(),
                containsInAnyOrder(
                        isReceiveState("DR-Antrag empfangen", isMessage("DR-Antrag", "DR-Antrag prüfen")),
                        isFunctionState("DR-Antrag prüfen", containsHeads(
                                isSendState("Genehmigen"),
                                isSendState("Ablehnen")
                        )),
                        isSendState("Genehmigen", "Mitarbeiter", "Genehmigung", "Buchung veranlassen"),
                        isSendState("Buchung veranlassen", "Reisestelle", "genehmigter DR-Antrag", "Ende"),
                        isSendState("Ablehnen", "Mitarbeiter", "Ablehnung",
                                isFunctionState("Ende")
                        ),
                        isFunctionState("Ende")
                ));

        assertThat("wrong states for 'Reisestelle' ", getSubject(result, "Reisestelle").getStates(),
                containsInAnyOrder(
                        isReceiveState("DR-Antrag empfangen", isMessage("genehmigter DR-Antrag", "Buchen")),
                        isFunctionState("Buchen", containsHeads(isFunctionState("Reise gebucht"))),
                        isFunctionState("Reise gebucht")
                ));

        assertThat("wrong objects", result.getObjects(),
                containsInAnyOrder(
                        isObjectName("DR-Antrag"),
                        isObjectName("Genehmigung"),
                        isObjectName("Ablehnung"),
                        isObjectName("Buchung"),
                        isObjectName("genehmigter DR-Antrag")
                ));
    }

    @Test
    public void testUnmarshalBookPage105() throws Exception {
        //given
        InputStream inputStream = ExampleModels.getBookPage105();

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(inputStream);

        //then
        assertThat(result.getName(), is("Dienstreiseantrag Seite/105"));
        assertThat(result.getVersion(), is(1));
        assertThat(result.getState(), is(ProcessModelState.ACTIVE));
        assertThat(result.getDescription(), not(emptyString()));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Mitarbeiter"),
                        isSubjectName("Vorgesetzter"),
                        isSubjectName("Reisestelle")
                ));

        assertThat("wrong states for 'Mitarbeiter' ", getSubject(result, "Mitarbeiter").getStates(),
                containsInAnyOrder(isFunctionState("DR-Antrag ausfüllen",
                        containsPermisssions(isPermission("DR-Antrag",
                                isFieldPermission("Name", Permission.WRITE, true),
                                isFieldPermission("Reisebeginn", Permission.WRITE, true),
                                isFieldPermission("Reiseende", Permission.WRITE, true),
                                isFieldPermission("Reiseziel", Permission.WRITE, true)
                        )
                        ),
                        containsHeads(isSendState("DR-Antrag an Vorgesetzer senden"))
                ),
                        isSendState("DR-Antrag an Vorgesetzer senden", "Vorgesetzter", "DR-Antrag", "Antwort von Vorgesetzter empfangen"),
                        isReceiveState("Antwort von Vorgesetzter empfangen", isMessage("Genehmigung", "DR antreten"), isMessage("Ablehnung", "DR-Antrag zurückziehen/ändern")),
                        isFunctionState("DR antreten", containsHeads(isFunctionState("DR beendet"))),
                        isFunctionState("DR beendet"),
                        isFunctionState("DR-Antrag zurückziehen/ändern",
                                containsPermisssions(isPermission("DR-Antrag",
                                        isFieldPermission("Name", Permission.READ, false),
                                        isFieldPermission("Reisebeginn", Permission.WRITE, true),
                                        isFieldPermission("Reiseende", Permission.WRITE, true),
                                        isFieldPermission("Reiseziel", Permission.WRITE, true)
                                )
                                ),
                                containsHeads(
                                        isSendState("DR-Antrag an Vorgesetzer senden"),
                                        isFunctionState("DR-Antrag zurückziehen")
                                )
                        ),
                        isFunctionState("DR-Antrag zurückziehen")
                ));

        assertThat("wrong states for 'Vorgesetzter' ", getSubject(result, "Vorgesetzter").getStates(),
                containsInAnyOrder(
                        isReceiveState("DR-Antrag empfangen", isMessage("DR-Antrag", "DR-Antrag prüfen")),
                        isFunctionState("DR-Antrag prüfen", containsHeads(
                                isSendState("Genehmigen"),
                                isSendState("Ablehnen")
                        )),
                        isSendState("Genehmigen", "Mitarbeiter", "Genehmigung", "Buchung veranlassen"),
                        isSendState("Buchung veranlassen", "Reisestelle", "genehmigter DR-Antrag", "Ende"),
                        isSendState("Ablehnen", "Mitarbeiter", "Ablehnung",
                                isFunctionState("Ende")
                        ),
                        isFunctionState("Ende")
                ));

        assertThat("wrong states for 'Reisestelle' ", getSubject(result, "Reisestelle").getStates(),
                containsInAnyOrder(
                        isReceiveState("DR-Antrag empfangen", isMessage("genehmigter DR-Antrag", "Buchen")),
                        isFunctionState("Buchen", containsHeads(isFunctionState("Reise gebucht"))),
                        isFunctionState("Reise gebucht")
                ));

        assertThat("wrong objects", result.getObjects(),
                containsInAnyOrder(
                        isObjectName("DR-Antrag"),
                        isObjectName("Genehmigung"),
                        isObjectName("Ablehnung"),
                        isObjectName("Buchung"),
                        isObjectName("genehmigter DR-Antrag")
                ));
    }

    @Test
    public void testUnmarshalDienstreiseantrag() throws Exception {
        //given
        InputStream inputStream = ExampleModels.getDienstreiseantrag();

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(inputStream);

        //then
        assertThat(result.getName(), is("Erweiterter Dienstreiseantrag"));
        assertThat(result.getVersion(), is(1));
        assertThat(result.getState(), is(ProcessModelState.ACTIVE));
        assertThat(result.getDescription(), not(emptyString()));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Mitarbeiter"),
                        isSubjectName("Vorgesetzter"),
                        isSubjectName("Reisestelle")
                ));

        assertThat("wrong states for 'Mitarbeiter' ", getSubject(result, "Mitarbeiter").getStates(),
                containsInAnyOrder(isFunctionState("DR-Antrag ausfüllen",
                        containsPermisssions(isPermission("DR-Antrag",
                                isFieldPermission("Name", Permission.WRITE, true, "${user.name}"),
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

        assertThat("wrong states for 'Vorgesetzter' ", getSubject(result, "Vorgesetzter").getStates(),
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

        assertThat("wrong states for 'Reisestelle' ", getSubject(result, "Reisestelle").getStates(),
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
                                isField("Name", FieldType.STRING),
                                isField("Reisebeginn", FieldType.DATE),
                                isField("Reiseende", FieldType.DATE),
                                isField("Reiseziel", FieldType.STRING),
                                isToMany("Mitreisende",
                                        isField("Antragsteller", FieldType.STRING),
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
    public void testUnmarshalRechungslegung() throws Exception {
        //given
        InputStream inputStream = ExampleModels.getRechungslegung();

        //when
        ProcessDefinition result = new ProcessModel().unmarshal(inputStream);

        //then
        assertThat(result.getName(), is("Rechnungslegung"));
        assertThat("wrong subjects", result.getSubjects(),
                containsInAnyOrder(
                        isStarterSubjectName("Mitarbeiter"),
                        isSubjectName("Druckservice")
                ));

        FunctionStateDefinition createPrintFunction
                = (FunctionStateDefinition) getState(result, "Druckservice", "Druck erstellen");

        assertThat("missing parameters for 'Druckservice/Druck erstellen'",
                createPrintFunction.getParameters(), hasEntry("layout", "Rechnung")
        );

        PermissionDefinition invoicePermission = getPermission(createPrintFunction, "Rechnung");

        assertThat("missing permission for 'Druckservice/Druck erstellen/Rechnung'", invoicePermission,
                isPermission("Rechnung",
                        isNestedPermission("Empfänger",
                                isFieldPermission("Kunde", Permission.READ, false)
                        ),
                        isFieldPermission("Datum", Permission.READ, false),
                        isFieldPermission("Nummer", Permission.READ, false),
                        isNestedPermission("Position",
                                isFieldPermission("Text", Permission.READ, false),
                                isFieldPermission("Wert", Permission.READ, false)
                        )
                )
        );

    }
}
