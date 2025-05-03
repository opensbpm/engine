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
 * ****************************************************************************
 */
package org.opensbpm.engine.core.junit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.hamcrest.Matcher;
import org.opensbpm.engine.api.instance.AbstractContainerAttributeSchema;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.AttributeSchemaVisitor;
import org.opensbpm.engine.api.instance.IndexedAttributeSchema;
import org.opensbpm.engine.api.instance.NestedAttributeSchema;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.SimpleAttributeSchema;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskRequest;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.springframework.data.util.Pair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.opensbpm.engine.utils.StreamUtils.filterToOne;

public class TestTask extends Task {

    private final Map<ObjectSchema, Pair<ObjectData, LazyDynaBeanImpl>> datas = new HashMap<>();

    public TestTask(TaskInfo taskInfo, TaskResponse taskResponse) {
        super(taskInfo, taskResponse);
    }

    public String getDisplayName(String objectName) {
        return getObjectData(objectName).getDisplayName()
                .orElse(null);
    }

    public Object getValue(String objectName, String expression) throws ReflectiveOperationException {
        return PropertyUtils.getProperty(getBean(objectName), expression);
    }

    public void setValue(String objectName, String expression, Object value) throws ReflectiveOperationException {
        PropertyUtils.setProperty(getBean(objectName), expression, value);
    }

    public TaskRequest createRequest(String stateName) {
        NextState nextState = filterToOne(getNextStates(), state
                -> state.getName().equals(stateName))
                .orElseThrow(() -> new IllegalStateException("State " + stateName + " not found in next-states, possible states " + getNextStatesAsString(this)));
        return createTaskRequest(nextState);
    }

    @Override
    public TaskRequest createTaskRequest(NextState nextState) {
        Objects.requireNonNull(nextState, "nextState must not be null");
        if (!getNextStates().contains(nextState)) {
            throw new IllegalArgumentException("nextStates doesn't contain " + nextState);
        }
        TaskRequest taskRequest = new TaskRequest(taskResponse.getId(),
                nextState,
                taskResponse.getLastChanged()
        );
        List<ObjectData> objectDatas = datas.values().stream()
                .map((pair) -> {
                    pair.getFirst().getData().putAll(pair.getSecond().getIdMap());
                    return pair.getFirst();
                }).collect(Collectors.toList());
        taskRequest.setObjectDatas(objectDatas);
        return taskRequest;
    }

    private ObjectData getObjectData(String name) {
        return getFromCache(name).getFirst();
    }

    private DynaBean getBean(String name) {
        return getFromCache(name).getSecond();
    }

    private Pair<ObjectData, LazyDynaBeanImpl> getFromCache(String name) {
        return datas.computeIfAbsent(getObjectSchema(name), schema
                -> {
            ObjectData objectData = getObjectData(schema);
            return Pair.of(objectData, createDynaBean(name, schema.getAttributes(), objectData.getData()));
        });
    }

    private ObjectSchema getObjectSchema(String name) {
        return filterToOne(getSchemas(),
                schema -> schema.getName().equals(name))
                .orElseThrow(() -> new IllegalArgumentException("ObjectSchema '" + name + "' not found"));
    }

    private static String getNextStatesAsString(Task task) {
        return task.getNextStates().stream()
                .map((NextState nextState) -> nextState.getName())
                .collect(Collectors.joining(","));
    }

    private static LazyDynaBeanImpl createDynaBean(String name, List<AttributeSchema> attributes, Map<Long, Serializable> data) {
        DynaClass dynaClass = createDynaClass(name, attributes);
        return new LazyDynaBeanImpl(dynaClass, attributes, data);
    }

    private static DynaClass createDynaClass(String name, List<AttributeSchema> attributes) {
        DynaProperty[] properties = attributes.stream()
                .map(attribute
                        -> attribute.accept(new AttributeSchemaVisitor<DynaProperty>() {
                    @Override
                    public DynaProperty visitSimple(SimpleAttributeSchema attributeSchema) {
                        return new DynaProperty(attributeSchema.getName(), attributeSchema.getType());
                    }

                    @Override
                    public DynaProperty visitNested(NestedAttributeSchema attributeSchema) {
                        return new DynaProperty(attribute.getName(), LazyDynaBean.class);
                    }

                    @Override
                    public DynaProperty visitIndexed(IndexedAttributeSchema attributeSchema) {
                        return new DynaProperty(attribute.getName(), List.class, LazyDynaBean.class);
                    }
                }))
                .toArray(DynaProperty[]::new);
        return new BasicDynaClass(name, null, properties);
    }

    public void assertNextStates(Matcher<NextState>... stateMatchers) {
        assertThat(String.format("wrong states for '%s'", getStateName()),
                getNextStates(), containsInAnyOrder(stateMatchers));
    }

    private static class LazyDynaBeanImpl extends LazyDynaBean {

        private final List<AttributeSchema> attributes;

        public LazyDynaBeanImpl(DynaClass dynaClass, List<AttributeSchema> attributes, Map<Long, Serializable> data) {
            super(dynaClass);
            this.attributes = attributes;
            data.replaceAll((t, u) -> {
                if (u instanceof Map) {
                    NestedAttributeSchema attribute = (NestedAttributeSchema) getAttribute(t);
                    return createDynaBean(attribute.getName(), attribute.getAttributes(), (Map<Long, Serializable>) u);
                } else if (u instanceof List) {
                    return (Serializable) ((List) u).stream()
                            .map(row -> {
                                IndexedAttributeSchema attribute = (IndexedAttributeSchema) getAttribute(t);
                                return createDynaBean(attribute.getName(), attribute.getAttributes(), (Map<Long, Serializable>) row);
                            })
                            .collect(Collectors.toList());
                } else {
                    return u;
                }

            });
            this.values = new NameIdMapDecorator(attributes, data);
        }

        private AttributeSchema getAttribute(Long id) {
            return filterToOne(attributes, attribute -> attribute.getId().equals(id))
                    .orElseThrow(() -> new IllegalArgumentException("Attribute " + id + " not found"));
        }

        public Map<Long, Serializable> getIdMap() {
            Map<Long, Serializable> data = new HashMap<>(((NameIdMapDecorator) values).store);
            data.replaceAll((t, u) -> {
                if (u instanceof LazyDynaBeanImpl) {
                    return (Serializable) ((LazyDynaBeanImpl) u).getIdMap();
                } else if (u instanceof List) {
                    return (Serializable) ((List<LazyDynaBeanImpl>) u).stream()
                            .map(LazyDynaBeanImpl.class::cast)
                            .map(bean -> bean.getIdMap())
                            .collect(Collectors.toList());
                } else {
                    return u;
                }
            });
            return data;
        }

        @Override
        protected Map<String, Object> newMap() {
            //field values are set in constructor
            return null;
        }

        @Override
        protected Object createDynaBeanProperty(String name, Class<?> type) {
            String attributeName = new DefaultResolver().getProperty(name);
            AbstractContainerAttributeSchema attributeSchema = filterToOne(attributes, attribute
                    -> attribute.getName().equals(attributeName))
                    .filter(attribute -> attribute instanceof AbstractContainerAttributeSchema)
                    .map(AbstractContainerAttributeSchema.class::cast)
                    .orElseThrow(() -> new IllegalArgumentException(name + " not AbstractContainerAttributeSchema"));
            int index = new DefaultResolver().getIndex(name);

            List<Map<Long, Serializable>> listData = (List<Map<Long, Serializable>>) values
                    .computeIfAbsent(attributeSchema.getName(), t -> new ArrayList<Map<Long, Serializable>>());
            Map<Long, Serializable> nestedData;
            if (index > 0 && index < listData.size()) {
                nestedData = listData.get(index);
            } else {
                nestedData = new HashMap<>();
            }
            return createDynaBean(attributeName, attributeSchema.getAttributes(), nestedData);
        }

        private class NameIdMapDecorator implements Map<String, Object>, Serializable {

            private final List<AttributeSchema> attributes;
            private final Map<Long, Serializable> store;

            public NameIdMapDecorator(List<AttributeSchema> attributes, Map<Long, Serializable> store) {
                this.attributes = Objects.requireNonNull(attributes);
                this.store = store;
            }

            private Long getId(String name) {
                return filterToOne(attributes, attribute -> attribute.getName().equals(name))
                        .orElseThrow(() -> new IllegalArgumentException("Attribute " + name + " not found"))
                        .getId();
            }

            private String getName(Long id) {
                return filterToOne(attributes, attribute -> attribute.getId().equals(id))
                        .orElseThrow(() -> new IllegalArgumentException("Attribute " + id + " not found"))
                        .getName();
            }

            @Override
            public int size() {
                return store.size();
            }

            @Override
            public boolean isEmpty() {
                return store.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return store.containsKey(getId((String) key));
            }

            @Override
            public boolean containsValue(Object value) {
                return store.containsValue(value);
            }

            @Override
            public Object get(Object key) {
                return store.get(getId((String) key));
            }

            @Override
            public Object put(String key, Object value) {
                return store.put(getId(key), (Serializable) value);
            }

            @Override
            public Object remove(Object key) {
                return store.remove(getId((String) key));
            }

            @Override
            public void putAll(Map<? extends String, ? extends Object> m) {
                m.forEach((key, value) -> {
                    store.put(getId((String) key), (Serializable) value);
                });
            }

            @Override
            public void clear() {
                store.clear();
            }

            @Override
            public Set<String> keySet() {
                return store.keySet().stream()
                        .map(id -> getName(id))
                        .collect(Collectors.toSet());
            }

            @Override
            public Collection<Object> values() {
                return store.values().stream()
                        .map(value -> (Object) value)
                        .collect(Collectors.toList());
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return store.entrySet().stream()
                        .map(entry -> new Entry<String, Object>() {
                    @Override
                    public String getKey() {
                        return getName(entry.getKey());
                    }

                    @Override
                    public Object getValue() {
                        return entry.getValue();
                    }

                    @Override
                    public Object setValue(Object value) {
                        return entry.setValue((Serializable) value);
                    }
                })
                        .collect(Collectors.toSet());
            }
        }

    }
}
