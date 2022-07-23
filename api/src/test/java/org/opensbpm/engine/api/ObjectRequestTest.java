package org.opensbpm.engine.api;

import java.util.Collections;
import org.junit.Test;
import org.opensbpm.engine.api.EngineService.ObjectRequest;
import org.opensbpm.engine.api.instance.ObjectSchema;
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
    
    @Test
    public void testOfWithSchemaCreatesInstance() throws Exception {
        //given
        ObjectSchema objectSchema = ObjectSchema.of(Long.MIN_VALUE, "name", Collections.emptyList());

        //when
        ObjectRequest objectRequest = ObjectRequest.of(objectSchema);

        //then
        assertThat("ObjectRequest not instantiated", objectRequest.getId(), is(objectSchema.getId()));
    }

}
