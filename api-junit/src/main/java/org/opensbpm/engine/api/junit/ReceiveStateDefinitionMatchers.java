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

import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition.ReceiveTransitionDefinition;

import static org.opensbpm.engine.api.junit.StateDefinitionMatchers.isStateName;

import java.util.Arrays;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;

public final class ReceiveStateDefinitionMatchers {

    public static Matcher<StateDefinition> isReceiveState(String name) {
        //isState(ReceiveStateDefinition.class, name);
        return allOf(instanceOf(ReceiveStateDefinition.class), isStateName(name));
    }

    public static Matcher<StateDefinition> isReceiveState(String name, Matcher<ReceiveTransitionDefinition>... messages) {
        //isState(ReceiveStateDefinition.class, name);
        return allOf(instanceOf(ReceiveStateDefinition.class), isStateName(name), hasTransitions(containsInAnyOrder(Arrays.asList(messages))));
    }

    public static Matcher<StateDefinition> hasTransitions(Matcher<Iterable<? extends ReceiveTransitionDefinition>> matcher) {
        return value(ReceiveStateDefinition.class, ReceiveStateDefinition::getTransitions, matcher);
    }

    public static Matcher<ReceiveTransitionDefinition> isMessage(String objectName, String stateName) {
        return allOf(
                value(ReceiveTransitionDefinition::getObjectDefinition, isObjectName(objectName)),
                value(ReceiveTransitionDefinition::getHead, isStateName(stateName))
        );
    }

    private ReceiveStateDefinitionMatchers() {
    }

}
