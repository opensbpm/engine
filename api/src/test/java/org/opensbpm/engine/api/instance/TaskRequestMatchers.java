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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.opensbpm.engine.api.adapters.MapAdapter.ValueElement;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.opensbpm.engine.api.junit.CommonMatchers.value;

public class TaskRequestMatchers {

    public static Matcher<ObjectData> isObjectData(String modelName, Matcher<ObjectData> fields) {
        return allOf(isModelName(modelName), fields);
    }

    public static Matcher<ObjectData> isObjectData(String modelName, Matcher<ObjectData> fields, Matcher<ObjectData> child) {
        return allOf(isModelName(modelName), fields, child);
    }

    public static Matcher<ObjectData> isModelName(String name) {
        return value(ObjectData::getName, is(name));
    }

    public static Matcher<ObjectData> containsFields(Matcher<Map<? extends Long, ? extends Serializable>>... entryMatcher) {
        return value(ObjectData::getData, allOf(entryMatcher));
    }

    public static Matcher<ObjectData> hasFields(Matcher<Iterable<? extends ValueElement>> itemsMatcher) {
        return new TypeSafeMatcher<ObjectData>() {
            @Override
            protected boolean matchesSafely(ObjectData item) {
                return itemsMatcher.matches(item.getData());
            }

            @Override
            public void describeTo(Description description) {
                contains(itemsMatcher).describeTo(description);
            }
        };
    }

    public static Matcher<Map<? extends Long, ? extends Serializable>> isValueElement(Long id, Serializable value) {
        return hasEntry(is(id), is(value));
    }

    private TaskRequestMatchers() {
    }

}
