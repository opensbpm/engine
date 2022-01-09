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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Task {

    private final TaskInfo taskInfo;
    protected final TaskResponse taskResponse;
    private final Map<ObjectSchema, ObjectBean> objectCache = new HashMap<>();

    public Task(TaskInfo taskInfo, TaskResponse taskResponse) {
        this.taskInfo = Objects.requireNonNull(taskInfo, "taskInfo must be non null");
        this.taskResponse = Objects.requireNonNull(taskResponse, "taskResponse must be non null");
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public Long getId() {
        return taskInfo.getId();
    }

    public String getProcessName() {
        return taskInfo.getProcessName();
    }

    public String getStateName() {
        return taskInfo.getStateName();
    }

    public LocalDateTime getLastChanged() {
        return taskResponse.getLastChanged();
    }

    public List<NextState> getNextStates() {
        return taskResponse.getNextStates();
    }

    public List<ObjectSchema> getSchemas() {
        return taskResponse.getSchemas();
    }

    /**
     * retrieve a {@link ObjectBean} for a {@link ObjectSchema}
     *
     * @param objectSchema objectSchema to retrieve a ObjectBean
     * @return an newly created or already instantiated ObjectBean
     */
    public ObjectBean getObjectBean(ObjectSchema objectSchema) {
        //TODO validate given objectSchema against taskResponse.getSchemas()
        return objectCache.computeIfAbsent(objectSchema, schema -> {
            Map<Long, Serializable> data = taskResponse.getDatas().stream()
                    .filter(objectData -> objectData.getName().equals(objectSchema.getName()))
                    .map(objectData -> objectData.getData())
                    .findFirst()
                    .orElse(new HashMap<>());
            return new ObjectBean(schema, new AttributeStore(objectSchema, new HashMap<>(data)));

        });
    }

    /**
     * create a TaskRequest for a given state
     */
    public TaskRequest createTaskRequest(NextState nextState) {
        Objects.requireNonNull(nextState, "nextState must be non null");
        if (!getNextStates().contains(nextState)) {
            throw new IllegalArgumentException("nextStates doesn't contain " + nextState);
        }
        List<ObjectData> objectDatas = taskResponse.getSchemas().stream()
                .map(objectSchema -> getObjectBean(objectSchema))
                .map(objectBean -> objectBean.createObjectData())
                .collect(Collectors.toList());
        return createTaskRequest(nextState, objectDatas);
    }

    protected final TaskRequest createTaskRequest(NextState nextState, List<ObjectData> objectDatas) {
        TaskRequest taskRequest = new TaskRequest(taskResponse.getId(),
                nextState,
                taskResponse.getLastChanged()
        );
        taskRequest.setObjectDatas(objectDatas);
        return taskRequest;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("taskInfo", taskInfo)
                .append("taskResponse", taskResponse)
                .toString();
    }

}
