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
package org.opensbpm.engine.core.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.opensbpm.engine.api.model.ObjectReference;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.IsAttributeParent;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import static org.opensbpm.engine.core.model.entities.AttributeModelVisitor.indexed;

/**
 * Bean representation of {@link AttributeModel}. The main purpose of this class is to provide a {@link Map} to easy
 * access all values of a {@link org.opensbpm.engine.core.engine.entities.ObjectInstance} in scripts.
 * Nested and indexed attributes are lazy created, so there is no need for additional creation of nested
 * {@code ObjectBean}s
 */
public class ObjectBean implements DynaBean {
    //TODO it seems ObjectEntityBean would be a better name, cause it uses only entities

    private final IsAttributeParent attributeParent;
    private final AttributeStore attributeStore;

    /**
     * instantiate a {@link ObjectBean} with the given attribute and store.
     *
     * @param attributeParent
     * @param attributeStore
     */
    public ObjectBean(IsAttributeParent attributeParent, AttributeStore attributeStore) {
        this.attributeParent = attributeParent;
        this.attributeStore = attributeStore;
    }

    public Collection<AttributeModel> getAttributeModels() {
        return attributeParent.getAttributeModels();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DynaClass getDynaClass() {
        DynaProperty[] dynaProperties = getAttributeModels().stream()
                .map(attributeModel -> attributeModel.accept(new AttributeModelVisitor<DynaProperty>() {
            @Override
            public DynaProperty visitSimple(SimpleAttributeModel simpleAttributeModel) {
                return new DynaProperty(attributeModel.getName(), simpleAttributeModel.getFieldType().getType());
            }

            @Override
            public DynaProperty visitReference(ReferenceAttributeModel attributeModel) {
                return new DynaProperty(attributeModel.getName(), ObjectReference.class);
            }

            @Override
            public DynaProperty visitNested(NestedAttributeModel nestedAttributeModel) {
                return new DynaProperty(attributeModel.getName(), ObjectBean.class);
            }

            @Override
            public DynaProperty visitIndexed(IndexedAttributeModel indexedAttributeModel) {
                return new DynaProperty(attributeModel.getName(), List.class, ObjectBean.class);
            }

        })).toArray(DynaProperty[]::new);
        return new BasicDynaClass(attributeParent.getName(), /*ObjectBean.class*/ null, dynaProperties);
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
        return findAttributeModel(name).accept(new AttributeModelVisitor<Object>() {
            @Override
            public Object visitSimple(SimpleAttributeModel attributeModel) {
                return attributeStore.getSimple(attributeModel);
            }

            @Override
            public Object visitReference(ReferenceAttributeModel attributeModel) {
                HashMap<String, String> value = attributeStore.getReference(attributeModel);
                return ObjectReference.of(value.get("id"), value.get("displayName"));

            }

            @Override
            public Object visitNested(NestedAttributeModel attributeModel) {
                return new ObjectBean(attributeModel, new AttributeStore(attributeModel, attributeStore.getNested(attributeModel)));
            }

            @Override
            public Object visitIndexed(IndexedAttributeModel attributeModel) {
                return attributeStore.getIndexed(attributeModel);
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
            ObjectBean attributeModelBean = findAttributeModel(name).accept(indexed())
                    .map(attributeModel -> new ObjectBean(attributeModel, new AttributeStore(attributeModel)))
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
        findAttributeModel(name).accept(new AttributeModelVisitor<Void>() {
            @Override
            public Void visitSimple(SimpleAttributeModel attributeModel) {
                attributeStore.put(attributeModel, (Serializable) value);
                return null;
            }

            @Override
            public Void visitReference(ReferenceAttributeModel attributeModel) {
                attributeStore.put(attributeModel, ((ObjectReference) value).toMap());
                return null;
            }

            @Override
            public Void visitNested(NestedAttributeModel attributeModel) {
                attributeStore.put(attributeModel, ((ObjectBean) value).attributeStore.getValues());
                return null;
            }

            @Override
            public Void visitIndexed(IndexedAttributeModel attributeModel) {
                @SuppressWarnings("unchecked")
                List<HashMap<Long, Serializable>> rawValue = ((List<ObjectBean>) value).stream()
                        .map(objectBean -> objectBean.attributeStore.getValues())
                        .collect(Collectors.toList());
                attributeStore.put(attributeModel, new ArrayList<>(rawValue));
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

    private AttributeModel findAttributeModel(String name) {
        return getAttributeModels().stream()
                .filter(attributeModel -> attributeModel.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such property " + name));
    }

}
