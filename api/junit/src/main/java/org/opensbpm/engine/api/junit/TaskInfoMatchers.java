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

import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.hamcrest.Matcher;

import static org.opensbpm.engine.api.junit.CommonMatchers.value;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;

public final class TaskInfoMatchers {

    public static Matcher<Iterable<? extends TaskInfo>> hasOneState(String stateName) {
        return contains(isStateName(stateName));
    }

    public static Matcher<TaskInfo> isStateName(String stateName) {
        return value(TaskInfo::getStateName, is(stateName));
    }

    public static Matcher<NextState> isNextState(String name, boolean end) {
        return allOf(value(NextState::getName, is(name)),
                value(NextState::isEnd, is(end))
        );
    }

    private TaskInfoMatchers() {
    }

}
