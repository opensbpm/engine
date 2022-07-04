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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.instance.SourceMap;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.IsAttributeParent;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.springframework.data.util.Pair;

/**
 * Encapsulates the persisted {@link AttributeModel} values in a type-safe API
 */
public class AttributeStore {

    private final IsAttributeParent attributeParent;
    private final HashMap<Long, Serializable> values;
    private final String id;

    public AttributeStore(IsAttributeParent attributeParent) {
        this(attributeParent, new HashMap<Long, Serializable>());
    }

    public AttributeStore(IsAttributeParent attributeParent, HashMap<Long, Serializable> values) {
        this.attributeParent = attributeParent;
        this.id = null;
        this.values = values;
    }

    public AttributeStore(ObjectModel attributeParent, SourceMap sourceMap) {
        this.attributeParent = attributeParent;
        this.id = sourceMap.getId();
        this.values = sourceMap.toIdMap();
    }

    public String getId() {
        return id;
    }

    HashMap<Long, Serializable> getValues() {
        return values;
    }

    public boolean isEmpty(SimpleAttributeModel attributeModel) {
        return getSimple(attributeModel) == null;
    }

    public Serializable getSimple(SimpleAttributeModel attributeModel) {
        return computeIfAbsent(attributeModel, modelId -> attributeModel.getDefaultValue());
    }

    public boolean isEmpty(ReferenceAttributeModel attributeModel) {
        return getReference(attributeModel) == null;
    }

    public HashMap<String, String> getReference(ReferenceAttributeModel attributeModel) {
        return computeIfAbsent(attributeModel, modelId -> null);
    }

    public boolean isEmpty(NestedAttributeModel attributeModel) {
        return getNested(attributeModel).isEmpty();
    }

    public HashMap<Long, Serializable> getNested(NestedAttributeModel attributeModel) {
        return computeIfAbsent(attributeModel, modelId -> new HashMap<>());
    }

    public boolean isEmpty(IndexedAttributeModel attributeModel) {
        return getIndexed(attributeModel).isEmpty();
    }

    public ArrayList<HashMap<Long, Serializable>> getIndexed(IndexedAttributeModel attributeModel) {
        return computeIfAbsent(attributeModel, modelId -> new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T computeIfAbsent(AttributeModel attributeModel, Function<Long, T> absentMapping) {
        return (T) values.computeIfAbsent(findAttributeModel(attributeModel).getId(), absentMapping);
    }

    public void put(SimpleAttributeModel attributeModel, Serializable value) {
        putValue(attributeModel, value);
    }

    public void put(ReferenceAttributeModel attributeModel, HashMap<String, String> value) {
        putValue(attributeModel, value);
    }

    public void put(NestedAttributeModel attributeModel, HashMap<Long, Serializable> value) {
        putValue(attributeModel, value);
    }

    public void put(IndexedAttributeModel attributeModel, ArrayList<HashMap<Long, Serializable>> value) {
        putValue(attributeModel, value);
    }

    private Serializable putValue(AttributeModel attributeModel, Serializable value) {
        return values.put(findAttributeModel(attributeModel).getId(), value);
    }

    private AttributeModel findAttributeModel(AttributeModel attributeModel) {
        return attributeParent.getAttributeModels().stream()
                .filter(attribute -> attribute.equalsId(attributeModel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("attribute " + attributeModel.getName()
                + " not found in " + attributeParent.getName()));
    }

    public HashMap<Long, Serializable> toIdMap(Predicate<AttributeModel> filter) {
        return new HashMap<>(attributeParent.getAttributeModels().stream()
                .filter(filter)
                .map(attributeModel -> {
                    Serializable serializable = attributeModel.accept(new AttributeModelVisitor<Serializable>() {
                        @Override
                        public Serializable visitSimple(SimpleAttributeModel attributeModel) {
                            return getSimple(attributeModel);
                        }

                        @Override
                        public Serializable visitReference(ReferenceAttributeModel attributeModel) {
                            return getReference(attributeModel);
                        }

                        @Override
                        public Serializable visitNested(NestedAttributeModel attributeModel) {
                            return new AttributeStore(attributeModel, getNested(attributeModel)).toIdMap(filter);
                        }

                        @Override
                        public Serializable visitIndexed(IndexedAttributeModel attributeModel) {
                            return new ArrayList<>(getIndexed(attributeModel).stream()
                                    .map(values -> new AttributeStore(attributeModel, values).toIdMap(filter))
                                    .collect(Collectors.toList()));
                        }
                    });
                    if (serializable == null) {
                        return null;
                    } else {
                        return Pair.of(attributeModel.getId(), serializable);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(pair -> pair.getFirst(), pair -> pair.getSecond()))
        );
    }

    public void updateValues(FunctionState state, Map<Long, Serializable> data) {
        AttributeStore dataStore = new AttributeStore(attributeParent, new HashMap<>(data));
        attributeParent.getAttributeModels().stream()
                .filter(attributeModel -> state.hasWritePermission(attributeModel))
                .forEach(attributeModel -> updateAttribute(state, attributeModel, dataStore));
    }

    private void updateAttribute(FunctionState state, AttributeModel attributeModel, AttributeStore data) {
        attributeModel.accept(new AttributeValueDeterminer(data, state))
                .ifPresent(value -> putValue(attributeModel, value));
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
    private static class AttributeValueDeterminer implements AttributeModelVisitor<Optional<Serializable>> {

        private final AttributeStore data;
        private final FunctionState state;

        private AttributeValueDeterminer(AttributeStore data, FunctionState state) {
            this.data = data;
            this.state = state;
        }

        @Override
        public Optional<Serializable> visitSimple(SimpleAttributeModel attributeModel) {
            validateMandatoryValue(attributeModel, "Attribute", () -> data.isEmpty(attributeModel));

            return Optional.ofNullable(data.getSimple(attributeModel));
        }

        @Override
        public Optional<Serializable> visitReference(ReferenceAttributeModel attributeModel) {
            validateMandatoryValue(attributeModel, "Nestedattribute", () -> data.isEmpty(attributeModel));

            return Optional.ofNullable(data.getReference(attributeModel));
        }

        @Override
        public Optional<Serializable> visitNested(NestedAttributeModel attributeModel) {
            validateMandatoryValue(attributeModel, "Nestedattribute", () -> data.isEmpty(attributeModel));

            if (!data.isEmpty(attributeModel)) {
                HashMap<Long, Serializable> nested = createNestedInstance(attributeModel, data.getNested(attributeModel));
                return Optional.ofNullable(nested);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Serializable> visitIndexed(IndexedAttributeModel attributeModel) {
            validateMandatoryValue(attributeModel, "Listattribute", () -> data.isEmpty(attributeModel));

            if (!data.isEmpty(attributeModel)) {
                ArrayList<HashMap<Long, Serializable>> indexed = new ArrayList<>();
                for (HashMap<Long, Serializable> nestedData : data.getIndexed(attributeModel)) {
                    indexed.add(createNestedInstance(attributeModel, nestedData));
                }
                return Optional.ofNullable(indexed);
            }
            return Optional.empty();
        }

        private void validateMandatoryValue(AttributeModel attributeModel, String prefix, Supplier<Boolean> hasValue) {
            if (state.isMandatory(attributeModel) && hasValue.get()) {
                throw new IllegalArgumentException(prefix + " " + attributeModel.getName() + " is mandatory, but not value given");
            }
        }

        private HashMap<Long, Serializable> createNestedInstance(NestedAttributeModel nestedModel, HashMap<Long, Serializable> nestedData) {
            HashMap<Long, Serializable> hashMap = new HashMap<>();

            new AttributeStore(nestedModel, hashMap).updateValues(state, nestedData);

            return hashMap;
        }
    }
}
