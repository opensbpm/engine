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
package org.opensbpm.engine.api.junit;

import static org.opensbpm.engine.api.junit.CommonMatchers.value;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObjectName;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isSubjectName;
import static org.opensbpm.engine.api.junit.StateDefinitionMatchers.isStateName;
import static org.opensbpm.engine.api.junit.StateDefinitionMatchers.isState;

import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.SendStateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import org.hamcrest.Matcher;

public final class SendStateDefinitionMatchers {

    public static Matcher<StateDefinition> isSendState(String name) {
        return isState(SendStateDefinition.class, name);
    }

    public static Matcher<StateDefinition> isSendState(String name, String receiverName, String objectName) {
        return isSendState(name, isSubjectName(receiverName), isObjectName(objectName));
    }

    public static Matcher<StateDefinition> isSendState(String name, String receiverName, String objectName, String headName) {
        return isSendState(name, isSubjectName(receiverName), isObjectName(objectName), isStateName(headName));
    }

    public static Matcher<StateDefinition> isSendState(String name, String receiverName, String objectName, Matcher<StateDefinition> head) {
        return isSendState(name, isSubjectName(receiverName), isObjectName(objectName), head);
    }

    private static Matcher<StateDefinition> isSendState(String name, Matcher<SubjectDefinition> receiver, Matcher<ObjectDefinition> objectModel) {
        return isState(SendStateDefinition.class, name, isReceiver(receiver), isObjectModel(objectModel));
    }

    private static Matcher<StateDefinition> isSendState(String name, Matcher<SubjectDefinition> receiver, Matcher<ObjectDefinition> objectModel, Matcher<StateDefinition> head) {
        return isState(SendStateDefinition.class, name, isReceiver(receiver), isObjectModel(objectModel), isHead(head));
    }

    private static Matcher<StateDefinition> isReceiver(Matcher<SubjectDefinition> matcher) {
        return value(SendStateDefinition.class, SendStateDefinition::getReceiver, matcher);
    }

    private static Matcher<StateDefinition> isObjectModel(Matcher<ObjectDefinition> matcher) {
        return value(SendStateDefinition.class, SendStateDefinition::getObjectModel, matcher);
    }

    private static Matcher<StateDefinition> isHead(Matcher<StateDefinition> matcher) {
        return value(SendStateDefinition.class, SendStateDefinition::getHead, matcher);
    }

    private SendStateDefinitionMatchers() {
    }

}
