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

import static org.opensbpm.engine.utils.StreamUtils.oneOrMoreAsList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import static org.hamcrest.CoreMatchers.is;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import org.hamcrest.TypeSafeMatcher;

public final class CommonMatchers {

    public static <T> Matcher<Collection<? extends T>> isEmpty() {
        return is(empty());
    }

    public static <T> Matcher<T> isTypeWith(Class<T> type, Matcher<? super T>... matchers) {
        Matcher<? super T> typeMatcher = instanceOf(type);
        List<Matcher<? super T>> allMatchers = oneOrMoreAsList(typeMatcher, matchers);
        return allOf(allMatchers);
    }

    /**
     * value(PermissionDefinition::isMandatory, is(true))
     *
     * @param <T>
     * @param <V>
     * @param valueProvider
     * @param objectMatcher
     * @return
     */
    public static <T, V> Matcher<T> value(Function<T, V> valueProvider, Matcher<V> objectMatcher) {
        return value((String) null, valueProvider, objectMatcher);
    }

    public static <T, V> Matcher<T> value(String valueDescription, Function<T, V> valueProvider, Matcher<V> objectMatcher) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T item) {
                return objectMatcher.matches(valueProvider.apply(item));
            }

            @Override
            public void describeTo(Description description) {
                Optional.ofNullable(valueDescription)
                        .ifPresent(text -> description.appendText(text).appendText(" "));
                objectMatcher.describeTo(description);
            }
        };
    }

    public static <R, T, V> Matcher<R> value(Class<T> type, Function<T, V> valueProvider, Matcher<V> matcher) {
        return new TypeSafeMatcher<R>(type) {
            @Override
            protected boolean matchesSafely(R item) {
                V value = valueProvider.apply(type.cast(item));
                return matcher.matches(value);
            }

            @Override
            public void describeTo(Description description) {
                matcher.describeTo(description);
            }
        };
    }

    private CommonMatchers() {
    }

}
