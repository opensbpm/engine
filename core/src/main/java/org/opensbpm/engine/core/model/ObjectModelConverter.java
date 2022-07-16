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
package org.opensbpm.engine.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opensbpm.engine.api.model.builder.ObjectBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AttributeBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ReferenceBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToManyBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToOneBuilder;
import org.opensbpm.engine.core.model.ObjectModelConverter.ObjectModelCache.ObjectCache;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.springframework.data.util.Pair;

class ObjectModelConverter {

    private ObjectModelCache objectModelCache;

    public ObjectModelCache convert(ProcessModel processModel) {
        Map<ObjectModel, ObjectCache> objects = processModel.getObjectModels().stream()
                .map(objectDefinition -> createObjectBuilder(objectDefinition))
                .collect(Pair.toMap());

        objectModelCache = new ObjectModelCache(objects);
        for (Map.Entry<ObjectModel, ObjectCache> entry : objects.entrySet()) {
            ObjectCache objectCache = entry.getValue();
            Map<AttributeModel, AttributeBuilder<?, ?>> attributes = createAttributes(entry.getKey(), objectCache);
            objectCache.putAll(attributes);
        }
        return objectModelCache;
    }

    private Pair<ObjectModel, ObjectCache> createObjectBuilder(ObjectModel objectModel) {
        ObjectBuilder objectBuilder = new ObjectBuilder(objectModel.getName());
        objectModel.getDisplayName().ifPresent(displayName -> objectBuilder.withDisplayName(displayName));
        return Pair.of(objectModel, new ObjectCache(objectBuilder));
    }

    private Map<AttributeModel, AttributeBuilder<?, ?>> createAttributes(ObjectModel objectModel, ObjectCache objectCache) {
        return objectModel.getAttributeModels().stream()
                .map(attributeModel -> createAttribute(objectCache, attributeModel))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private Pair<AttributeModel, AttributeBuilder<?, ?>> createAttribute(ObjectCache objectCache, AttributeModel attributeModel) {
        AttributeBuilder<?, ?> attributeBuilder = createAttributeBuilder(objectCache, attributeModel);
        objectCache.objectBuilder.addAttribute(attributeBuilder);
        return Pair.of(attributeModel, attributeBuilder);
    }

    private AttributeBuilder<?, ?> createAttributeBuilder(ObjectCache objectCache, AttributeModel attributeModel) {
        return attributeModel.accept(new AttributeModelVisitor<AttributeBuilder<?, ?>>() {
            @Override
            public FieldBuilder visitSimple(SimpleAttributeModel simpleAttributeModel) {
                FieldBuilder fieldBuilder = new FieldBuilder(simpleAttributeModel.getName(), simpleAttributeModel.getFieldType());
                if (simpleAttributeModel.isIndexed()) {
                    fieldBuilder.asIndexed();
                }
                return fieldBuilder;
            }

            @Override
            public ReferenceBuilder visitReference(ReferenceAttributeModel referenceAttributeModel) {
                return new ReferenceBuilder(referenceAttributeModel.getName(), objectModelCache.getObjectBuilder(referenceAttributeModel.getReference()));
            }

            @Override
            public ToOneBuilder visitNested(NestedAttributeModel nestedAttributeModel) {
                ToOneBuilder toOneBuilder = new ToOneBuilder(nestedAttributeModel.getName());
                nestedAttributeModel.getAttributeModels().forEach(attributeModel -> {
                    AttributeBuilder<?, ?> attributeBuilder = createAttributeBuilder(objectCache, attributeModel);
                    toOneBuilder.addAttribute(attributeBuilder);
                    objectCache.put(attributeModel, attributeBuilder);
                });
                return toOneBuilder;
            }

            @Override
            public ToManyBuilder visitIndexed(IndexedAttributeModel indexedAttributeModel) {
                ToManyBuilder toManyBuilder = new ToManyBuilder(indexedAttributeModel.getName());
                indexedAttributeModel.getAttributeModels().forEach(attributeModel -> {
                    AttributeBuilder<?, ?> attributeBuilder = createAttributeBuilder(objectCache, attributeModel);
                    toManyBuilder.addAttribute(attributeBuilder);
                    objectCache.put(attributeModel, attributeBuilder);
                });
                return toManyBuilder;
            }
        });
    }

    public static class ObjectModelCache {

        private final Map<ObjectModel, ObjectCache> objects;

        public ObjectModelCache(Map<ObjectModel, ObjectCache> objects) {
            this.objects = objects;
        }

        public Stream<ObjectBuilder> getObjectBuilders() {
            return objects.values().stream()
                    .map(oc -> oc.getObjectBuilder());
        }

        public ObjectBuilder getObjectBuilder(ObjectModel objectModel) {
            return objects.get(objectModel).getObjectBuilder();
        }

        public AttributeBuilder<?, ?> getAttributeBuilder(ObjectModel objectModel, AttributeModel attributeModel) {
            return objects.get(objectModel).get(attributeModel);
        }

        public ObjectModel findObjectModel(AttributeModel attributeModel) {
            for (Map.Entry<ObjectModel, ObjectCache> entry : objects.entrySet()) {
                if (entry.getValue().containsKey(attributeModel)) {
                    return entry.getKey();
                }
            }
            throw new IllegalStateException("no ObjectModel for attribute " + attributeModel);
        }

        static class ObjectCache {

            private final ObjectBuilder objectBuilder;
            private Map<AttributeModel, AttributeBuilder<?, ?>> cache = new HashMap<>();

            public ObjectCache(ObjectBuilder objectBuilder) {
                super();
                this.objectBuilder = objectBuilder;
            }

            private ObjectBuilder getObjectBuilder() {
                return objectBuilder;
            }

            private void put(AttributeModel attributeModel, AttributeBuilder<?, ?> attributeBuilder) {
                cache.put(attributeModel, attributeBuilder);
            }

            private void putAll(Map<AttributeModel, AttributeBuilder<?, ?>> attributes) {
                cache.putAll(attributes);
            }

            private AttributeBuilder<?, ?> get(AttributeModel attributeModel) {
                return cache.get(attributeModel);
            }

            private boolean containsKey(AttributeModel attributeModel) {
                return cache.containsKey(attributeModel);
            }
        }

    }
}
