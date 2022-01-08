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

import org.opensbpm.engine.api.model.definition.StateDefinition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public final class StateDefinitionMatchers {

    /**
     * Hamcrest {@link Matcher} to check {@link StateDefinition}
     * @param type Implementation of {@link StateDefinition}
     * @param name {@link StateDefinition#name}
     * @param matchers additional matchers
     * @return 
     */
    public static Matcher<StateDefinition> isState(Class<? extends StateDefinition> type, String name,
            Matcher<? super StateDefinition>... matchers) {
        List<Matcher<? super StateDefinition>> all = new ArrayList<>();
        all.addAll(Arrays.asList(instanceOf(type), isStateName(name)));
        all.addAll(Arrays.asList(matchers));
        return allOf(all);
    }

    /**
     * Hamcrest {@link Matcher} to check {@link StateDefinition}
     * @param type Implementation of {@link StateDefinition}
     * @param name {@link StateDefinition#name}
     * @param displayName {@link StateDefinition#name}
     * @param matchers additional matchers
     * @return 
     */
    public static Matcher<StateDefinition> isState(Class<? extends StateDefinition> type, String name,
            String displayName, Matcher<? super StateDefinition>... matchers) {
        List<Matcher<? super StateDefinition>> all = new ArrayList<>();
        all.addAll(Arrays.asList(instanceOf(type), isStateName(name), isDisplayName(displayName)));
        all.addAll(Arrays.asList(matchers));
        return allOf(all);
    }

    public static Matcher<StateDefinition> isStateName(String name) {
        return value(StateDefinition::getName, is(name));
    }

    /**
     * Hamcrest {@link Matcher} to check {@link StateDefinition#displayName}
     * @param displayName {@link StateDefinition#displayName}
     * @return 
     */
    public static Matcher<StateDefinition> isDisplayName(String displayName) {
        return value(StateDefinition::getDisplayName, is(displayName));
    }

    private StateDefinitionMatchers() {
    }

}
