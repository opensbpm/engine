package org.opensbpm.engine.utils;

import org.opensbpm.engine.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 * @author stefan
 */
public class CollectionUtilsTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testReadonlyCollectionWithNull() {
        //given
        List<String> list = null;

        //when
        Collection<String> result = CollectionUtils.readonlyCollection(list);

        //then
        assertThat(result, is(notNullValue()));
        result.add("new String");
        fail("adding a new element to readonly collection must throw UnsupportedOperationException");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReadonlyCollectionWithList() {
        //given
        List<String> list = new ArrayList<>();

        //when
        Collection<String> result = CollectionUtils.readonlyCollection(list);

        //then
        assertThat(result, is(notNullValue()));
        result.add("new String");
        fail("adding a new element to readonly collection must throw UnsupportedOperationException");
    }

    @Test
    public void testAddWithNull() {
        //given
        List<String> list = null;
        
        //when
        List<String> result = CollectionUtils.add(list, "new String");
        
        //then
        assertThat(result, contains("new String"));
    }
    @Test
    public void testAddWithList() {
        //given
        List<String> list = new ArrayList<>();
        
        //when
        List<String> result = CollectionUtils.add(list, "new String");
        
        //then
        assertThat(result, contains("new String"));
    }

}
