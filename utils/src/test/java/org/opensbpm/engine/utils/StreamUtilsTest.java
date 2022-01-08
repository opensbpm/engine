package org.opensbpm.engine.utils;

import org.opensbpm.engine.utils.StreamUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

@RunWith(Theories.class)
public class StreamUtilsTest {

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
