package org.opensbpm.engine.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import static org.hamcrest.Matchers.anEmptyMap;
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
        Map<String,String> values = StreamUtils.emptyOrUnmodifiableMap(null);
        assertThat(values, is(anEmptyMap()));

        //when + then
        assertThrows(UnsupportedOperationException.class, () -> {
            values.put("a","b");
        });
    }

    @Test
    public void testEmptyOrUnmodifiableMapWithValue() throws Exception {
        //given
        Map<String,String> values = new HashMap<>();
        values.put("a","b");

        //when + then
        Map<String,String> result = StreamUtils.emptyOrUnmodifiableMap(values);
        assertThrows(UnsupportedOperationException.class, () -> {
            result.put("a","b");
        });
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

    private static final String SUBTRACT = "subtract";

    @DataPoints(SUBTRACT)
    public static SubtractData[] SUBTRACT_DATA = new SubtractData[]{
        new SubtractData(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
        new SubtractData(Collections.emptyList(), Arrays.asList(1), Collections.emptyList()),
        new SubtractData(Arrays.asList(1), Collections.emptyList(), Arrays.asList(1)),
        new SubtractData(Arrays.asList(1), Arrays.asList(1), Collections.emptyList()),
        new SubtractData(Arrays.asList(1, 2), Arrays.asList(2), Arrays.asList(1))
    };

    public static final class SubtractData {

        private final Collection<Integer> subtrahend;
        private final Collection<Integer> minuend;
        private final Collection<Integer> result;

        private SubtractData(Collection<Integer> subtrahend, Collection<Integer> minuend, Collection<Integer> result) {
            this.subtrahend = subtrahend;
            this.minuend = minuend;
            this.result = result;
        }

    }

    @Theory
    @Test
    public void testSubtract(final @FromDataPoints(SUBTRACT) SubtractData data) throws Exception {
        //given

        //when
        Collection<Integer> result = StreamUtils.subtract(data.subtrahend, data.minuend);

        //then
        assertThat(result, is(equalTo(data.result)));
    }

}
