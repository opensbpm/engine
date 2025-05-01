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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import static org.opensbpm.engine.api.instance.AttributeSchemaVisitor.indexed;

/**
 * Bean representation of {@link IsAttributesContainer}. The main purpose of this
 * class is to provide a {@link DynaBean} implementation to access all values of
 * a {@link ObjectData} in scripts. Nested and indexed attributes are lazy
 * created, so there is no need for additional creation of nested {@code ObjectBean}s
 */
public class ObjectBean implements DynaBean {

    /**
     * Instantiate a {@link ObjectBean} with the given attributes and SourceMap.
     *
     * @param objectSchema to create the {@link ObjectBean}
     * @param sourceMap id-based Map, must match the attributesContainer
     * @return ready to use {@link ObjectBean} instance
     */
    public static ObjectBean from(ObjectSchema objectSchema, SourceMap sourceMap) {
        //TODO validate values
        return new ObjectBean(objectSchema, AttributeStore.of(objectSchema, sourceMap));
    }

    /**
     * Instantiate a {@link ObjectBean} with the given attributes and values.
     *
     * @param attributesContainer to create the {@link ObjectBean}
     * @param values id-based Map, must match the attributesContainer
     * @return ready to use {@link ObjectBean} instance
     */
    public static ObjectBean from(IsAttributesContainer attributesContainer, Map<Long, Serializable> values) {
        //TODO validate values
        return new ObjectBean(attributesContainer, new AttributeStore(attributesContainer, new HashMap<>(values)));
    }

    private final IsAttributesContainer attributesContainer;
    private final AttributeStore attributeStore;

    /**
     * instantiate a {@link ObjectBean} with the given attribute and store.
     *
     * @param attributesContainer container for bean
     * @param attributeStore store for attributes
     * @deprecated
     */
    public ObjectBean(IsAttributesContainer attributesContainer, AttributeStore attributeStore) {
        this.attributesContainer = Objects.requireNonNull(attributesContainer, "attributesContainer must not be null");
        this.attributeStore = Objects.requireNonNull(attributeStore, "attributeStore must not be null");
    }

    public ObjectBean(IsAttributesContainer attributesContainer) {
        this.attributesContainer = Objects.requireNonNull(attributesContainer, "attributesContainer must not be null");
        this.attributeStore = new AttributeStore(attributesContainer);
    }

    public String getName() {
        return attributesContainer.getName();
    }

    public String getId() {
        return attributeStore.getId();
    }

    public Collection<AttributeSchema> getAttributeModels() {
        return attributesContainer.getAttributes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DynaClass getDynaClass() {
        DynaProperty[] dynaProperties = getAttributeModels().stream()
                .map(attributeSchema -> attributeSchema.accept(new AttributeSchemaVisitor<DynaProperty>() {
            @Override
            public DynaProperty visitSimple(SimpleAttributeSchema simpleAttributeSchema) {
                return new DynaProperty(attributeSchema.getName(), simpleAttributeSchema.getFieldType().getType());
            }

            @Override
            public DynaProperty visitNested(NestedAttributeSchema nestedAttributeSchema) {
                return new DynaProperty(attributeSchema.getName(), ObjectBean.class);
            }

            @Override
            public DynaProperty visitIndexed(IndexedAttributeSchema indexedAttributeSchema) {
                return new DynaProperty(attributeSchema.getName(), List.class, ObjectBean.class);
            }

        })).toArray(DynaProperty[]::new);
        return new BasicDynaClass(attributesContainer.getName(), /*ObjectBean.class*/ null, dynaProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(String name, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String name) {
        return get(findAttributeSchema(name));
    }

    public Object get(AttributeSchema attribute) {
        return attribute.accept(new AttributeSchemaVisitor<Object>() {
            @Override
            public Object visitSimple(SimpleAttributeSchema attributeSchema) {
                return attributeStore.getSimple(attributeSchema);
            }

            @Override
            public Object visitNested(NestedAttributeSchema attributeSchema) {
                return new ObjectBean(attributeSchema, new AttributeStore(attributeSchema, attributeStore.getNested(attributeSchema)));
            }

            @Override
            public Object visitIndexed(IndexedAttributeSchema attributeSchema) {
                List<ObjectBean> indexed = new ArrayList<>();

                List<HashMap<Long, Serializable>> rawValues = attributeStore.getIndexed(attributeSchema);
                for (HashMap<Long, Serializable> rawMap : rawValues) {
                    indexed.add(new ObjectBean(attributeSchema, new AttributeStore(attributeSchema, rawMap)));
                }
                return indexed;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String name, int index) {
        @SuppressWarnings("unchecked")
        List<ObjectBean> value = (List<ObjectBean>) get(name);
        if (value.size() <= index) {
            ObjectBean indexedBean = findAttributeSchema(name).accept(indexed())
                    .map(indexedSchema -> new ObjectBean(indexedSchema))
                    .orElseThrow(() -> new IllegalArgumentException("No such property " + name));
            value.add(index, indexedBean);
            set(findAttributeSchema(name), value);
        }
        return value.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(String name, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(String name, Object value) {
        set(findAttributeSchema(name), value);
    }

    public void set(AttributeSchema attribute, Object value) {
        attribute.accept(new AttributeSchemaVisitor<Void>() {
            @Override
            public Void visitSimple(SimpleAttributeSchema attributeSchema) {
                attributeStore.putSimple(attributeSchema, (Serializable) value);
                return null;
            }

            @Override
            public Void visitNested(NestedAttributeSchema attributeSchema) {
                attributeStore.putNested(attributeSchema, ((ObjectBean) value).attributeStore.getValues());
                return null;
            }

            @Override
            public Void visitIndexed(IndexedAttributeSchema attributeSchema) {
                @SuppressWarnings("unchecked")
                List<HashMap<Long, Serializable>> rawValue = ((List<ObjectBean>) value).stream()
                        .map(objectBean -> objectBean.attributeStore.getValues())
                        .collect(Collectors.toList());
                attributeStore.putIndexed(attributeSchema, new ArrayList<>(rawValue));
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(String name, int index, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(String name, String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String name, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private AttributeSchema findAttributeSchema(String name) {
        return getAttributeModels().stream()
                .filter(attributeSchema -> attributeSchema.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such property " + name));
    }

    public Map<Long, Serializable> toIdMap() {
        return attributeStore.toIdMap(m -> true);
    }

}
