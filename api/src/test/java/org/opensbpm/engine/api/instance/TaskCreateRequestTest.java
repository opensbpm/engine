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
 *****************************************************************************
 */
package org.opensbpm.engine.api.instance;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.opensbpm.engine.api.instance.TaskRequestMatchers.containsFields;
import static org.opensbpm.engine.api.instance.TaskRequestMatchers.isObjectData;
import static org.opensbpm.engine.api.instance.TaskRequestMatchers.isValueElement;

public class TaskCreateRequestTest {

    @Test
    public void testCreateTaskRequest() {
        //given
        final long sId = 1l;
        final NextState nextState = NextState.of(sId, "Next State");

        final long o1f1mId = 11l;
        final long o1f2mId = 12l;
        final long o1f3mId = 13l;
        final long o1f4mId = 14l;
        final ObjectSchema object1Schema = createSchema("Object 1", o1f1mId, o1f2mId, o1f3mId, o1f4mId);
        ObjectData o1Response = createData(object1Schema, "1.2");

        //Child of Object 2
        final long o2sf1mId = 211l;
        final long o2sf2mId = 212l;
        final long o2sf3mId = 213l;
        final long o2sf4mId = 214l;
        final ObjectSchema object2SubSchema = createSchema("Object 2 Sub", o2sf1mId, o2sf2mId, o2sf3mId, o2sf4mId);
        ObjectData o2sResponse = createData(object2SubSchema, "2.1.2");

        final long o2f1mId = 21l;
        final long o2f2mId = 22l;
        final long o2f3mId = 23l;
        final long o2f4mId = 24l;
        final ObjectSchema object2Schema = createSchema("Object 2", o2f1mId, o2f2mId, o2f3mId, o2f4mId, new ObjectData[]{o2sResponse});
        ObjectData o2Response = createData(object2Schema, "2.2", o2sResponse);

        final LocalDateTime lastChanged = LocalDateTime.MIN;
        TaskResponse taskResponse = TaskResponse.of(sId,
                Arrays.asList(nextState),
                lastChanged,
                asList(object1Schema, object2Schema),
                asList(o1Response, o2Response)
        );
        Logger.getLogger(getClass().getName()).log(Level.INFO, "taskResponse:{0}", taskResponse);

        Task task = new Task(new TaskInfo(), taskResponse);
        task.getObjectBean(object1Schema).set("Field 3", "1.3");
        task.getObjectBean(object1Schema).set("Field 4", "1.4");

        task.getObjectBean(object2Schema).set("Field 3", "2.3");
        task.getObjectBean(object2Schema).set("Field 4", "2.4");

//        getChildObject(task, "Object 2", "Object 2 Sub").getAttribute("Field 3").setValue("2.1.3");
//        getChildObject(task, "Object 2", "Object 2 Sub").getAttribute("Field 4").setValue("2.1.4");

        //when
        TaskRequest result = task.createTaskRequest(nextState);

        //then
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(sId));
        assertThat(result.getObjectData(), containsInAnyOrder(
                isObjectData("Object 1", containsFields(
                        //TODO rethink: isValueElement(o1f1mId/*"Field 1"*/, null),
                        isValueElement(o1f2mId/*""Field 2"*/, "1.2"),
                        isValueElement(o1f3mId/*""Field 3"*/, "1.3"),
                        isValueElement(o1f4mId/*""Field 4"*/, "1.4")
                )),
                isObjectData("Object 2", containsFields(
                        //TODO rethink: isValueElement(o2f1mId/*""Field 1"*/, null),
                        isValueElement(o2f2mId/*"Field 2"*/, "2.2"),
                        isValueElement(o2f3mId/*"Field 3"*/, "2.3"),
                        isValueElement(o2f4mId/*"Field 4"*/, "2.4")
                )
                //                        ,containsChilds(isObjectData("Object 2 Sub", containsFields(
                //                                isValueElement(o2sf1mId/*"Field 1"*/, null),
                //                                isValueElement(o2sf2mId/*"Field 2"*/, "2.1.2"),
                //                                isValueElement(o2sf3mId/*"Field 3"*/, "2.1.3"),
                //                                isValueElement(o2sf4mId/*"Field 4"*/, "2.1.4")
                //                        )))
                )
        ));
        assertThat(result.getNextState(), is(nextState));
        assertThat(result.getLastChanged(), is(lastChanged));
    }

    private ObjectSchema createSchema(String name, long f1mId, long f2mId, long f3mId, long f4mId, ObjectData... childs) {
        ObjectSchema objectDefinition = ObjectSchema.of(1l, name, Arrays.asList(
                SimpleAttributeSchema.of(f1mId, "Field 1", FieldType.STRING),
                SimpleAttributeSchema.of(f2mId, "Field 2", FieldType.STRING),
                SimpleAttributeSchema.of(f3mId, "Field 3", FieldType.STRING),
                SimpleAttributeSchema.of(f4mId, "Field 4", FieldType.STRING)
        )
        //              ,createChildDefs(childs)
        );
        return objectDefinition;
    }

    private ObjectData createData(ObjectSchema objectSchema, String f2Value, ObjectData... childs) {
        ObjectBean objectBean = new ObjectBean(objectSchema);
        objectBean.set(objectSchema.getAttributes().get(0), null);
        objectBean.set(objectSchema.getAttributes().get(1), f2Value);
        objectBean.set(objectSchema.getAttributes().get(2), null);
        objectBean.set(objectSchema.getAttributes().get(3), "overriden");
        return ObjectData.from(objectBean);
    }

}
