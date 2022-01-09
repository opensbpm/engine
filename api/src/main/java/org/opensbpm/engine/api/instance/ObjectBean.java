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
 * Bean representation of {@link ObjectSchema}. The main purpose of this
 * class is to provide a {@link Map} to easy access all values of a
 * {@link org.opensbpm.engine.core.engine.entities.ObjectInstance}
 * in scripts. Nested and indexed attributes are lazy created, so there is no
 * need for additional creation of nested {@code ObjectBean}s
 */
public class ObjectBean implements DynaBean {

    private final IsAttributesContainer attributesContainer;
    private final AttributeStore attributeStore;

    /**
     * instantiate a {@link ObjectBean} with the given attribute and store.
     *
     * @param attributesContainer
     * @param attributeStore
     */
    public ObjectBean(IsAttributesContainer attributesContainer, AttributeStore attributeStore) {
        this.attributesContainer = Objects.requireNonNull(attributesContainer, "attributesContainer must be non null");
        this.attributeStore = Objects.requireNonNull(attributeStore, "attributeStore must be non null");
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
            public DynaProperty visitSimple(AttributeSchema simpleAttributeSchema) {
                return new DynaProperty(attributeSchema.getName(), simpleAttributeSchema.getFieldType().getType());
            }

//            @Override
//            public DynaProperty visitReference(ReferenceAttributeSchema attributeSchema) {
//                return new DynaProperty(attributeSchema.getName(), ObjectReference.class);
//            }
            @Override
            public DynaProperty visitNested(NestedAttributeSchema nestedAttributeSchema) {
                return new DynaProperty(attributeSchema.getName(), ObjectBean.class);
            }

            @Override
            public DynaProperty visitIndexed(NestedAttributeSchema indexedAttributeSchema) {
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
            public Object visitSimple(AttributeSchema attributeSchema) {
                return attributeStore.getSimple(attributeSchema);
            }

//            @Override
//            public Object visitReference(ReferenceAttributeModel attributeSchema) {
//                HashMap<String, String> value = attributeStore.getReference(attributeSchema);
//                return ObjectReference.of(value.get("id"), value.get("displayName"));
//
//            }
            @Override
            public Object visitNested(NestedAttributeSchema attributeSchema) {
                return new ObjectBean(attributeSchema, new AttributeStore(attributeSchema, attributeStore.getNested(attributeSchema)));
            }

            @Override
            public Object visitIndexed(NestedAttributeSchema attributeSchema) {
                return attributeStore.getIndexed(attributeSchema);
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
            ObjectBean attributeModelBean = findAttributeSchema(name).accept(indexed())
                    .map(attributeSchema -> new ObjectBean(attributeSchema, new AttributeStore(attributeSchema)))
                    .orElseThrow(() -> new IllegalArgumentException("No such property " + name));
            value.add(index, attributeModelBean);
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
            public Void visitSimple(AttributeSchema attributeSchema) {
                attributeStore.put(attributeSchema, (Serializable) value);
                return null;
            }

//            @Override
//            public Void visitReference(ReferenceAttributeModel attributeSchema) {
//                attributeStore.put(attributeSchema, ((ObjectReference) value).toMap());
//                return null;
//            }
            @Override
            public Void visitNested(NestedAttributeSchema attributeSchema) {
                attributeStore.put(attributeSchema, ((ObjectBean) value).attributeStore.getValues());
                return null;
            }

            @Override
            public Void visitIndexed(NestedAttributeSchema attributeSchema) {
                @SuppressWarnings("unchecked")
                List<HashMap<Long, Serializable>> rawValue = ((List<ObjectBean>) value).stream()
                        .map(objectBean -> objectBean.attributeStore.getValues())
                        .collect(Collectors.toList());
                attributeStore.put(attributeSchema, new ArrayList<>(rawValue));
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

    ObjectData createObjectData() {
        return ObjectData.of(attributesContainer.getName())
                .withData(attributeStore.getValues())
                .build();
    }
}
