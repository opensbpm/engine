package org.opensbpm.engine.api;

import org.junit.Test;
import org.opensbpm.engine.api.EngineService.ObjectRequest;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObjectRequestTest {

    @Test
    public void creatingOfObjectRequestCreatesInstance() throws Exception {
        //given
        long id = Long.MIN_VALUE;

        //when
        ObjectRequest objectRequest = ObjectRequest.of(id);

        //then
        assertThat("ObjectRequest no instantiated", objectRequest.getId(), is(id));
    }

}
