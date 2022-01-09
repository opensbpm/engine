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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.FieldType;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.toOne;

public class Task {

    private final TaskInfo taskInfo;
    protected final TaskResponse taskResponse;
    private final Map<ObjectSchema, AttributeStore> storeCache = new HashMap<>();
    private TaskDocument taskDocument;

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

    /**
     * create a new {@link AttributeStore} with the data from
     * {@link TaskResponse}
     *
     * @param objectSchema
     * @return a newly constructed {@link AttributeStore}
     */
    public AttributeStore createAttributeStore(ObjectSchema objectSchema) {
        return storeCache.computeIfAbsent(objectSchema, schema -> {
            Map<Long, Serializable> data = taskResponse.getDatas().stream()
                    .filter(objectData -> objectData.getName().equals(objectSchema.getName()))
                    .map(objectData -> objectData.getData())
                    .findFirst()
                    .orElse(new HashMap<>());
            return new AttributeStore(objectSchema, new HashMap<>(data));
        });
    }

    public TaskRequest createTaskRequest(NextState nextState, ObjectSchema objectSchema, AttributeStore attributeStore) {
        Objects.requireNonNull(nextState, "nextState must be non null");
        if (!getNextStates().contains(nextState)) {
            throw new IllegalArgumentException("nextStates doesn't contain " + nextState);
        }
        TaskRequest taskRequest = new TaskRequest(taskResponse.getId(),
                nextState,
                taskResponse.getLastChanged()
        );
        taskRequest.setObjectDatas(Arrays.asList(
                ObjectData.of(objectSchema.getName())
                        .withData(attributeStore.getValues())
                        .build()
        ));
        return taskRequest;
    }

    public TaskDocument getTaskDocument() {
        if (taskDocument == null) {
            taskDocument = new TaskDocument(taskResponse);
        }
        return taskDocument;
    }

    public TaskRequest createTaskRequest(NextState nextState) {
        Objects.requireNonNull(nextState, "nextState must be non null");
        if (!getNextStates().contains(nextState)) {
            throw new IllegalArgumentException("nextStates doesn't contain " + nextState);
        }
        TaskRequest taskRequest = new TaskRequest(taskResponse.getId(),
                nextState,
                taskResponse.getLastChanged()
        );
        taskRequest.setObjectDatas(getTaskDocument().createDataRequest());
        return taskRequest;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("taskInfo", taskInfo)
                .append("taskResponse", taskResponse)
                .toString();
    }

    /**
     * @deprecated use {@link ObjectBean} with {@link AttributeStore} instead
     */
    @Deprecated
    public class TaskDocument {

        private final Map<ObjectSchema, ObjectData> datas = new HashMap<>();

        private final TaskResponse taskResponse;

        private TaskDocument(TaskResponse taskResponse) {
            this.taskResponse = taskResponse;
        }

        public List<ObjectSchema> getSchemas() {
            return unmodifiableList(taskResponse.getSchemas());
        }

        public boolean hasData() {
            return !taskResponse.getDatas().isEmpty();
        }

        public ObjectData getData(ObjectSchema objectSchema) {
            final ObjectData objectData;
            if (datas.containsKey(objectSchema)) {
                objectData = datas.get(objectSchema);
            } else {
                objectData = taskResponse.getDatas().stream()
                        .filter(data -> data.getName().equals(objectSchema.getName()))
                        .reduce(toOne())
                        .orElse(ObjectData.of(objectSchema.getName())
                                .withData(new HashMap<>())
                                .build());
                datas.put(objectSchema, objectData);
            }
            return objectData;
        }

        public AttributeBean getAttribute(ObjectSchema objectSchema, AttributeSchema attributeSchema) {
            return new AttributeBean(attributeSchema, getData(objectSchema).getData());
        }

        public AttributeBean getIdAttribute(ObjectSchema objectSchema) {
            AttributeSchema idAttribute = objectSchema.getIdAttribute()
                    .orElseThrow(() -> new IllegalStateException(
                    format("Object schema with name '%s' does not have an id attribute schema.",
                            objectSchema.getName())));

            return getAttribute(objectSchema, idAttribute);
        }

        private List<ObjectData> createDataRequest() {
            return getSchemas().stream()
                    .map(objectSchema -> getData(objectSchema))
                    .collect(Collectors.toList());
        }
    }

    /**
     * @deprecated use {@link ObjectBean} with {@link AttributeStore} instead
     */
    @Deprecated
    public static class AttributeBean {

        private final Map<Long, Serializable> data;
        private final AttributeSchema attributeSchema;

        public AttributeBean(AttributeSchema attributeSchema, Map<Long, Serializable> data) {
            this.data = data;
            this.attributeSchema = attributeSchema;
        }

        public AttributeSchema getAttributeSchema() {
            return attributeSchema;
        }

        public String getName() {
            return attributeSchema.getName();
        }

        public FieldType getFieldType() {
            return attributeSchema.getFieldType();
        }

        public Class<?> getType() {
            return attributeSchema.getType();
        }

        public boolean isReadonly() {
            return attributeSchema.isReadonly();
        }

        public boolean isRequired() {
            return attributeSchema.isRequired();
        }

        public Serializable getValue() {
            return data.getOrDefault(attributeSchema.getId(), null);
        }

        public void setValue(Serializable value) {
            Serializable checkedValue = Optional.ofNullable(value)
                    .map(v -> (Serializable) getType().cast(v))
                    .orElse(null);
            data.put(attributeSchema.getId(), checkedValue);
        }

        public AttributeBean getAttribute(AttributeSchema nestedSchema) {
            Map<Long, Serializable> childData = (Map<Long, Serializable>) data.computeIfAbsent(attributeSchema.getId(), (t) -> {
                return new HashMap<>();
            });
            return new AttributeBean(nestedSchema, childData);
        }

    }
}
