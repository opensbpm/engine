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
import java.util.Collections;
import java.util.logging.Logger;
import org.junit.Test;
import org.opensbpm.engine.api.model.FieldType;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.fail;
import static org.opensbpm.engine.api.instance.TaskRequestMatchers.containsFields;
import static org.opensbpm.engine.api.instance.TaskRequestMatchers.isObjectData;
import static org.opensbpm.engine.api.instance.TaskRequestMatchers.isValueElement;

public class TaskTest {

    @Test
    public void testDelegateMethods() {
        //given
        final long sId = 1l;
        final LocalDateTime lastChanged = LocalDateTime.now();

        final TaskInfo taskInfo = new TaskInfo(sId, Long.MIN_VALUE, "processName", "stateName", LocalDateTime.MIN);
        final TaskResponse taskResponse = TaskResponse.of(Long.MIN_VALUE,
                Collections.emptyList(), lastChanged,
                Collections.emptyList(), Collections.emptyList()
        );

        //when
        Task task = new Task(taskInfo, taskResponse);

        //then
        assertThat(task.toString(), task.getId(), is(sId));
        assertThat(task.toString(), task.getProcessName(), is("processName"));
        assertThat(task.toString(), task.getStateName(), is("stateName"));
        assertThat(task.toString(), task.getLastChanged(), is(lastChanged));
        assertThat(task.toString(), task.getSchemas(), is(empty()));
    }

    @Test
    public void testGetObjectDataWithExistingData() {
        //given
        final long sId = 1l;
        NextState nextState = NextState.of(sId, "Next State");

        AttributeSchema attributeSchema = SimpleAttributeSchema.of(11l, "Attribute 1", FieldType.STRING);

        ObjectSchema objectSchema = ObjectSchema.of(1l, "Object 1", Arrays.asList(attributeSchema));
        //use ObjectBean to easily create repsonse-data map
        ObjectBean givenData = new ObjectBean(objectSchema);
        givenData.set("Attribute 1", "Data");

        TaskResponse taskResponse = TaskResponse.of(sId,
                Arrays.asList(nextState),
                LocalDateTime.MIN,
                asList(objectSchema),
                asList(ObjectData.from(givenData))
        );
        Logger.getLogger(getClass().getName()).info("taskResponse:" + taskResponse);

        Task task = new Task(new TaskInfo(), taskResponse);

        //when
        ObjectData objectData = task.getObjectData(objectSchema);

        //then
        assertThat("Two calls of Task.getObjectData must return the same instance",
                objectData, is(sameInstance(task.getObjectData(objectSchema))));
        
        assertThat(objectData.getData().get(11l), is("Data"));
    }

    @Test
    public void testGetObjectBeanStoreWithoutData() {
        //given
        final long sId = 1l;
        NextState nextState = NextState.of(sId, "Next State");

        AttributeSchema attributeSchema = SimpleAttributeSchema.of(11l, "Attribute 1", FieldType.STRING);

        ObjectSchema objectSchema = ObjectSchema.of(1l, "Object 1", Arrays.asList(attributeSchema));

        TaskResponse taskResponse = TaskResponse.of(sId,
                Arrays.asList(nextState),
                LocalDateTime.MIN,
                asList(objectSchema),
                Collections.emptyList()
        );
        Logger.getLogger(getClass().getName()).info("taskResponse:" + taskResponse);

        Task task = new Task(new TaskInfo(), taskResponse);

        //when
        ObjectBean objectBean = task.getObjectBean(objectSchema);

        //then
        assertThat(objectBean, is(notNullValue()));
        assertThat(objectBean.get(attributeSchema), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTaskRequestWithWrongState() {
        //given        
        TaskResponse taskResponse = TaskResponse.of(Long.MIN_VALUE,
                Collections.emptyList(),
                LocalDateTime.MIN,
                Collections.emptyList(),
                Collections.emptyList());
        Task task = new Task(new TaskInfo(), taskResponse);

        //when
        TaskRequest result = task.createTaskRequest(new NextState());

        //then
        fail("createTaskRequest with unknown nextState must throw IllegalArgumentException but was " + result);
    }

    @Test
    public void testCreateTaskRequestWithObjectData() {
        //given
        long id = 0l;
        NextState nextState = NextState.of(++id, "Next State");

        ObjectSchema objectSchema = ObjectBeanHelper.createObjetSchema();

        TaskResponse taskResponse = TaskResponse.of(Long.MIN_VALUE,
                asList(nextState),
                LocalDateTime.MIN,
                asList(objectSchema),
                Collections.emptyList());
        Task task = new Task(new TaskInfo(), taskResponse);

        //when
        task.getObjectData(objectSchema).getData().put(1l, "aString");

        //then
        TaskRequest taskRequest = task.createTaskRequest(nextState);
        assertThat(taskRequest.getObjectData(), containsInAnyOrder(
                isObjectData("root", containsFields(
                        isValueElement(1l/*"Field simple"*/, "aString")
                ))
        ));
        assertThat(taskRequest.getNextState(), is(nextState));

    }
    
    @Test
    public void testCreateTaskRequestWithObjectBean() {
        //given
        long id = 0l;
        NextState nextState = NextState.of(++id, "Next State");

        ObjectSchema objectSchema = ObjectBeanHelper.createObjetSchema();

        TaskResponse taskResponse = TaskResponse.of(Long.MIN_VALUE,
                asList(nextState),
                LocalDateTime.MIN,
                asList(objectSchema),
                Collections.emptyList());
        Task task = new Task(new TaskInfo(), taskResponse);

        //when
        task.getObjectBean(objectSchema).set("string", "aString");

        //then
        TaskRequest taskRequest = task.createTaskRequest(nextState);
        assertThat(taskRequest.getObjectData(), containsInAnyOrder(
                isObjectData("root", containsFields(
                        isValueElement(0l/*"Field simple"*/, "aString")
                ))
        ));
        assertThat(taskRequest.getNextState(), is(nextState));

    }
}
