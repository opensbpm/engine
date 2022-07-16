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
package org.opensbpm.engine.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThrows;

@RunWith(Theories.class)
public class StreamUtilsTest {

    @Test
    public void testEmptyOrUnmodifiableListWithNull() throws Exception {
        //given
        List<String> values = StreamUtils.emptyOrUnmodifiableList(null);
        assertThat(values, is(empty()));

        //when + then
        assertThrows(UnsupportedOperationException.class, () -> {
            values.add("b");
        });
    }

    @Test
    public void testEmptyOrUnmodifiableListWithValue() throws Exception {
        //given
        List<String> values = StreamUtils.oneOrMoreAsList("a");

        //when + then
        List<String> result = StreamUtils.emptyOrUnmodifiableList(values);
        assertThrows(UnsupportedOperationException.class, () -> {
            result.add("b");
        });
    }

    @Test
    public void testEmptyOrUnmodifiableSetWithNull() throws Exception {
        //given
        Set<String> values = StreamUtils.emptyOrUnmodifiableSet(null);
        assertThat(values, is(empty()));

        //when + then
        assertThrows(UnsupportedOperationException.class, () -> {
            values.add("b");
        });
    }

    @Test
    public void testEmptyOrUnmodifiableSetWithValue() throws Exception {
        //given
        Set<String> values = new HashSet<>(StreamUtils.oneOrMoreAsList("a"));

        //when + then
        Set<String> result = StreamUtils.emptyOrUnmodifiableSet(values);
        assertThrows(UnsupportedOperationException.class, () -> {
            result.add("b");
        });
    }

    @Test
    public void testEmptyOrUnmodifiableMapWithNull() throws Exception {
        //given
        Map<String, String> values = StreamUtils.emptyOrUnmodifiableMap(null);
        assertThat(values, is(anEmptyMap()));

        //when + then
        assertThrows(UnsupportedOperationException.class, () -> {
            values.put("a", "b");
        });
    }

    @Test
    public void testEmptyOrUnmodifiableMapWithValue() throws Exception {
        //given
        Map<String, String> values = new HashMap<>();
        values.put("a", "b");

        //when + then
        Map<String, String> result = StreamUtils.emptyOrUnmodifiableMap(values);
        assertThrows(UnsupportedOperationException.class, () -> {
            result.put("a", "b");
        });
    }

    @Test
    public void testLazyAddList() throws Exception {
        //given
        List<String> baseList = (List<String>) null;

        //when
        List<String> values = StreamUtils.lazyAdd(baseList, "a");

        //then
        assertThat(values, contains("a"));
    }

    @Test
    public void testLazyAddSet() throws Exception {
        //given
        Set<String> baseSet = (Set<String>) null;

        //when
        Set<String> values = StreamUtils.lazyAdd(baseSet, "a");

        //then
        assertThat(values, contains("a"));
    }

    @Test
    public void testFilterToOneWithIterable() throws Exception {
        //given
        Iterable<String> iterable = asList("a", "b", "c");

        //when
        Optional<String> values = StreamUtils.filterToOne(iterable, item -> item.equals("a"));

        //then
        assertThat(values.get(), is("a"));
    }

    @Test
    public void testFilterToOneWithStream() throws Exception {
        //given        
        Stream<String> stream = asList("a", "b", "c").stream();

        //when
        Optional<String> values = StreamUtils.filterToOne(stream, item -> item.equals("a"));

        //then
        assertThat(values.get(), is("a"));
    }

    private static final String ONEORMOREASLIST = "oneOrMoreAsList";
    @DataPoints(ONEORMOREASLIST)
    public static OneOrMoreAsListData[] ONEORMOREASLIST_DATA = new OneOrMoreAsListData[]{
        new OneOrMoreAsListData(1, new Integer[]{}, Arrays.asList(1)),
        new OneOrMoreAsListData(1, new Integer[]{2}, Arrays.asList(1, 2))
    };

    public static final class OneOrMoreAsListData {

        private final Integer arg1;
        private final Integer[] varargs;
        private final Collection<Integer> result;

        public OneOrMoreAsListData(Integer arg1, Integer[] varargs, Collection<Integer> result) {
            this.arg1 = arg1;
            this.varargs = varargs;
            this.result = result;
        }

    }

    @Theory
    @Test
    public void testOneOrMoreAsList(final @FromDataPoints(ONEORMOREASLIST) OneOrMoreAsListData data) throws Exception {
        //given

        //when        
        List<Integer> result = StreamUtils.oneOrMoreAsList(data.arg1, data.varargs);

        //then
        assertThat(result, is(equalTo(data.result)));
    }

    @Test
    public void testMapToList() {
        System.out.println("mapToList");
        Object object = new Object();
        //given
        Iterable<Object> from = Arrays.asList(object);

        //when
        List<Object> result = StreamUtils.mapToList(from, t -> {
            return t;
        });
        //then

        assertThat(result, hasItem(object));
    }

}
