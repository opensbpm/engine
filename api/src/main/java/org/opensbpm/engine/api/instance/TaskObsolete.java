package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.model.FieldType;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.toOne;

@Deprecated
public class TaskObsolete extends Task {

    private TaskDocument taskDocument;

    public TaskObsolete(TaskInfo taskInfo, TaskResponse taskResponse) {
        super(taskInfo, taskResponse);
    }

    /**
     *
     * @return
     * @deprecated use {@link TaskDocument#of(org.opensbpm.engine.api.instance.TaskResponse)} instead
     */
    @Deprecated
    public TaskDocument getTaskDocument() {
        if (taskDocument == null) {
            taskDocument = new TaskDocument(taskResponse);
        }
        return taskDocument;
    }

    @Deprecated
    public TaskRequest createTaskRequest(NextState nextState) {
        Objects.requireNonNull(nextState, "nextState must be non null");
        if (!getNextStates().contains(nextState)) {
            throw new IllegalArgumentException("nextStates doesn't contain " + nextState);
        }
        return createTaskRequest(nextState, getTaskDocument().createDataRequest());
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
                objectData = getObjectData(objectSchema);
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
