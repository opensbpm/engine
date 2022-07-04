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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Encapsulates the persisted {@link AttributeModel} values in a type-safe API
 */
public class AttributeStore {

    private final IsAttributesContainer attributeContainer;
    private final HashMap<Long, Serializable> values;
    private final String id;

    public AttributeStore(IsAttributesContainer attributeContainer) {
        this(attributeContainer, new HashMap<Long, Serializable>());
    }

    public AttributeStore(IsAttributesContainer attributeContainer, HashMap<Long, Serializable> values) {
        this.attributeContainer = attributeContainer;
        this.id = null;
        this.values = values;
    }

    public AttributeStore(ObjectSchema attributeContainer, SourceMap sourceMap) {
        this.attributeContainer = attributeContainer;
        this.id = sourceMap.getId();
        this.values = sourceMap.toIdMap();
    }

    public String getId() {
        return id;
    }

    HashMap<Long, Serializable> getValues() {
        return values;
    }

    public Serializable getSimple(/*Simple*/AttributeSchema attributeSchema) {
        //PENDING not sure what to do with default-value?
        return computeIfAbsent(attributeSchema, id -> /*attributeSchema.getDefaultValue()*/ null);
    }

//    public HashMap<String, String> getReference(ReferenceAttributeModel attributeModel) {
//        return computeIfAbsent(attributeModel, id -> null);
//    }
    public HashMap<Long, Serializable> getNested(NestedAttributeSchema attributeSchema) {
        return computeIfAbsent(attributeSchema, id -> new HashMap<>());
    }

    public ArrayList<HashMap<Long, Serializable>> getIndexed(NestedAttributeSchema attributeSchema) {
        return computeIfAbsent(attributeSchema, id -> new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T computeIfAbsent(AttributeSchema attributeSchema, Function<Long, T> absentMapping) {
        return (T) values.computeIfAbsent(findAttributeSchema(attributeSchema).getId(), absentMapping);
    }

    public void put(/*Simple*/AttributeSchema attributeSchema, Serializable value) {
        putValue(attributeSchema, value);
    }

//    public void put(ReferenceAttributeModel attributeModel, HashMap<String,String> value) {
//        putValue(attributeModel, value);
//    }
    public void put(NestedAttributeSchema attributeSchema, HashMap<Long, Serializable> value) {
        putValue(attributeSchema, value);
    }

    public void put(/*Indexed*/AttributeSchema attributeSchema, ArrayList<HashMap<Long, Serializable>> value) {
        putValue(attributeSchema, value);
    }

    private Serializable putValue(AttributeSchema attributeSchema, Serializable value) {
        return values.put(findAttributeSchema(attributeSchema).getId(), value);
    }

    private AttributeSchema findAttributeSchema(AttributeSchema attributeSchema) {
        return attributeContainer.getAttributes().stream()
                .filter(attribute -> attribute.getId().equals(attributeSchema.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("attribute '" + attributeSchema.getName() + "' not found in '" + attributeContainer.getName() + "'"));
    }

    public HashMap<Long, Serializable> toIdMap(Predicate<AttributeSchema> filter) {
        return new HashMap<>(attributeContainer.getAttributes().stream()
                .filter(filter)
                .map(attributeSchema -> {
                    Serializable serializable = attributeSchema.accept(new AttributeSchemaVisitor<Serializable>() {
                        @Override
                        public Serializable visitSimple(/*Simple*/AttributeSchema attributeSchema) {
                            return getSimple(attributeSchema);
                        }

//                        @Override
//                        public Serializable visitReference(ReferenceAttributeSchema attributeSchema) {
//                            return getReference(attributeSchema);
//                        }
                        @Override
                        public Serializable visitNested(NestedAttributeSchema attributeSchema) {
                            return new AttributeStore(attributeSchema, getNested(attributeSchema)).toIdMap(filter);
                        }

                        @Override
                        public Serializable visitIndexed(/*Indexed*/NestedAttributeSchema attributeSchema) {
                            return new ArrayList<>(getIndexed(attributeSchema).stream()
                                    .map(values -> new AttributeStore(attributeSchema, values).toIdMap(filter))
                                    .collect(Collectors.toList()));
                        }
                    });
                    if (serializable == null) {
                        return null;
                    } else {
                        return Pair.of(attributeSchema.getId(), serializable);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(pair -> pair.getKey(), pair -> pair.getValue()))
        );
    }

    public void updateValues(Map<Long, Serializable> data) {
        AttributeStore dataStore = new AttributeStore(attributeContainer, new HashMap<>(data));
        attributeContainer.getAttributes().stream()
                .forEach(attributeModel -> updateAttribute(attributeModel, dataStore));
    }

    private void updateAttribute(AttributeSchema attributeSchema, AttributeStore data) {
        attributeSchema.accept(new AttributeValueDeterminer(data))
                .ifPresent(value -> put(attributeSchema, value));
    }

    //not in use for now
//    public Map<String, Object> toNamedMap() {
//        return new AbstractMap<String, Object>() {
//            @Override
//            public Set<Map.Entry<String, Object>> entrySet() {
//                return attributeParent.getAttributeModels().stream()
//                        .map(attributeModel -> {
//                            Object value = attributeModel.accept(new AttributeModelVisitor<Object>() {
//                                @Override
//                                public Object visitSimple(SimpleAttributeModel attributeModel) {
//                                    return getSimple(attributeModel);
//                                }
//
//                                @Override
//                                public Object visitNested(NestedAttributeModel attributeModel) {
//                                    return new AttributeStore(attributeModel, getNested(attributeModel)).toNamedMap();
//                                }
//
//                                @Override
//                                public Object visitIndexed(IndexedAttributeModel attributeModel) {
//                                    return getIndexed(attributeModel).stream()
//                                            .map(values -> new AttributeStore(attributeModel, values).toNamedMap())
//                                            .collect(Collectors.toList());
//                                }
//                            });
//                            return toEntry(attributeModel, value);
//                        })
//                        .collect(Collectors.toSet());
//            }
//
//            private Map.Entry<String, Object> toEntry(AttributeModel attributeModel, Object value) {
//                return new Map.Entry<String, Object>() {
//                    @Override
//                    public String getKey() {
//                        return attributeModel.getName();
//                    }
//
//                    @Override
//                    public Object getValue() {
//                        return value;
//                    }
//
//                    @Override
//                    public Object setValue(Object value) {
//                        throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                };
//            }
//
//            @Override
//            public Object put(String key, Object value) {
//                return findModelByName(key).accept(new AttributeModelVisitor<Object>() {
//                    @Override
//                    public Object visitSimple(SimpleAttributeModel attributeModel) {
//                        return putValue(attributeModel, (Serializable) value);
//                    }
//
//                    @Override
//                    public Object visitNested(NestedAttributeModel attributeModel) {
//                        return putValue(attributeModel, (Serializable) value);
//                    }
//
//                    @Override
//                    public Object visitIndexed(IndexedAttributeModel attributeModel) {
//                        return putValue(attributeModel, (Serializable) value);
//                    }
//                });
//            }
//
//            private AttributeModel findModelById(Long id) {
//                return attributeParent.getAttributeModels().stream()
//                        .filter(attribute -> attribute.getId().equals(id))
//                        .findFirst()
//                        .orElseThrow(() -> new IllegalArgumentException("attribute with id" + id
//                        + " not found in " + attributeParent.getName()));
//            }
//
//            private AttributeModel findModelByName(String name) {
//                return attributeParent.getAttributeModels().stream()
//                        .filter(attribute -> attribute.getName().equals(name))
//                        .findFirst()
//                        .orElseThrow(() -> new IllegalArgumentException("attribute with name" + name
//                        + " not found in " + attributeParent.getName()));
//            }
//
//        };
//    }
    private static class AttributeValueDeterminer implements AttributeSchemaVisitor<Optional<Serializable>> {

        private final AttributeStore data;

        private AttributeValueDeterminer(AttributeStore data) {
            this.data = data;
        }

        @Override
        public Optional<Serializable> visitSimple(/*Simple*/AttributeSchema attributeModel) {
            Serializable value = data.getSimple(attributeModel);
            validateRequiredValue("Attribute", attributeModel, () -> value == null);
            
            return Optional.of(value);
        }
//            @Override
//            public Void visitReference(ReferenceAttributeModel attributeModel) {
//                HashMap<String, String> value = data.getReference(attributeModel);
//                validateRequiredValue("Nestedattribute", attributeSchema, () -> value == null);          
//                put(attributeModel, value);
//                return null;
//            }

        @Override
        public Optional<Serializable> visitNested(NestedAttributeSchema attributeSchema) {
            HashMap<Long, Serializable> nestedData = data.getNested(attributeSchema);
            validateRequiredValue("Nestedattribute", attributeSchema, () -> nestedData.isEmpty());
            
            if (!nestedData.isEmpty()) {
                HashMap<Long, Serializable> nested = createNestedInstance(attributeSchema, nestedData);
                return Optional.of(nested);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Serializable> visitIndexed(NestedAttributeSchema attributeSchema) {
            List<HashMap<Long, Serializable>> listData = data.getIndexed(attributeSchema);
            validateRequiredValue("Listattribute", attributeSchema, () -> listData.isEmpty());
            
            if (!listData.isEmpty()) {
                ArrayList<HashMap<Long, Serializable>> data = new ArrayList<>();
                for (HashMap<Long, Serializable> nestedData : listData) {
                    data.add(createNestedInstance(attributeSchema, nestedData));
                }
                return Optional.of(data);
            }
            return Optional.empty();
        }

        private void validateRequiredValue(String prefix, AttributeSchema attributeModel, Supplier<Boolean> valueCheck) throws IllegalArgumentException {
            if (needRequiredValue(attributeModel, valueCheck)) {
                throw newIllegalArgumentException(attributeModel, prefix);
            }
        }

        private boolean needRequiredValue(AttributeSchema attributeSchema, Supplier<Boolean> hasValue) {
            return attributeSchema.isRequired() && hasValue.get();
        }

        private IllegalArgumentException newIllegalArgumentException(AttributeSchema attributeModel, String prefix) {
            return new IllegalArgumentException(prefix + " " + attributeModel.getName() + " is mandatory, but not value given");
        }

        private HashMap<Long, Serializable> createNestedInstance(NestedAttributeSchema nestedModel, HashMap<Long, Serializable> nestedData) {
            HashMap<Long, Serializable> hashMap = new HashMap<>();
            new AttributeStore(nestedModel, hashMap).updateValues(nestedData);
            return hashMap;
        }
    }
}
