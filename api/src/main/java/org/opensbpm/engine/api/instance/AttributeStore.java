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
import org.opensbpm.engine.utils.PairUtils;

/**
 * Encapsulates the persisted {@link AttributeSchema} values in a type-safe API
 */
public class AttributeStore {

    /**
     * create a new {@link AttributeStore} for the given {@link ObjectSchema} and {@link SourceMap}.
     *
     * @param objectSchema the schema to define the valid attributes
     * @param sourceMap the sourceMap is used for the values,; must match the ObjectSchema
     * @return a new instance with the given parameters
     */
    public static AttributeStore of(ObjectSchema objectSchema, SourceMap sourceMap) {
        Objects.requireNonNull(objectSchema, "attributeContainer must not be null");
        Objects.requireNonNull(sourceMap, "sourceMap must not be null");
        return new AttributeStore(objectSchema,
                sourceMap.getId(),
                sourceMap.toIdMap());
    }

    private final IsAttributesContainer attributeContainer;
    private final HashMap<Long, Serializable> values;
    private final String id;

    /**
     * create a new store for the given attributeContainer.
     */
    public AttributeStore(IsAttributesContainer attributeContainer) {
        this(attributeContainer, new HashMap<>());
    }

    public AttributeStore(IsAttributesContainer attributeContainer, HashMap<Long, Serializable> values) {
        this.attributeContainer = attributeContainer;
        this.id = null;
        this.values = values;
    }

    private AttributeStore(IsAttributesContainer attributeContainer, String id, HashMap<Long, Serializable> values) {
        this.attributeContainer = attributeContainer;
        this.id = id;
        this.values = values;
    }

    public String getId() {
        return id;
    }

    HashMap<Long, Serializable> getValues() {
        return values;
    }

    public Serializable getSimple(/*Simple*/AttributeSchema attributeSchema) {
        return computeIfAbsent(attributeSchema, attrId -> attributeSchema.getDefaultValue()
                .orElse(null));
    }

    public HashMap<Long, Serializable> getNested(NestedAttributeSchema attributeSchema) {
        return computeIfAbsent(attributeSchema, attrId -> new HashMap<>());
    }

    public ArrayList<HashMap<Long, Serializable>> getIndexed(IndexedAttributeSchema attributeSchema) {
        return computeIfAbsent(attributeSchema, attrId -> new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T computeIfAbsent(AttributeSchema attributeSchema, Function<Long, T> absentMapping) {
        return (T) values.computeIfAbsent(findAttributeSchema(attributeSchema).getId(), absentMapping);
    }

    public void putSimple(/*Simple*/AttributeSchema attributeSchema, Serializable value) {
        putValue(attributeSchema, value);
    }

    public void putNested(NestedAttributeSchema attributeSchema, HashMap<Long, Serializable> value) {
        putValue(attributeSchema, value);
    }

    public void putIndexed(/*Indexed*/AttributeSchema attributeSchema, ArrayList<HashMap<Long, Serializable>> value) {
        putValue(attributeSchema, value);
    }

    private Serializable putValue(AttributeSchema attributeSchema, Serializable value) {
        AttributeSchema attribute = findAttributeSchema(attributeSchema);
        return values.put(attribute.getId(), validate(attribute, value));
    }

    private Serializable validate(AttributeSchema attribute, Serializable value) {
        return attribute.accept(new AttributeSchemaVisitor<Serializable>() {
            @Override
            public Serializable visitSimple(SimpleAttributeSchema attributeSchema) {
                validateRequiredValue(attributeSchema, () -> value == null);
                //TODO cast to correct type doesn't work correctly in ExampleProcessDienstreiseantragIT
//                Serializable checkedValue = Optional.ofNullable(value)
//                        .map(v -> (Serializable) attributeSchema.getFieldType().getType().cast(v))
//                        .orElse(null);
//                return checkedValue;
                return value;
            }

            @Override
            public Serializable visitNested(NestedAttributeSchema attributeSchema) {
                validateRequiredValue(attributeSchema, () -> value == null);
                return value;
            }

            @Override
            public Serializable visitIndexed(IndexedAttributeSchema attributeSchema) {
                validateRequiredValue(attributeSchema, () -> value == null);
                return value;
            }

            private void validateRequiredValue(AttributeSchema attributeModel, Supplier<Boolean> valueCheck) throws IllegalArgumentException {
                if (needRequiredValue(attributeModel, valueCheck)) {
                    throw newIllegalArgumentException(attributeModel);
                }
            }

            private boolean needRequiredValue(AttributeSchema attributeSchema, Supplier<Boolean> hasValue) {
                return attributeSchema.isRequired() && hasValue.get();
            }

            private IllegalArgumentException newIllegalArgumentException(AttributeSchema attributeModel) {
                return new IllegalArgumentException(attributeModel.getName() + " is mandatory, but not value given");
            }

        });
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
                        public Serializable visitSimple(SimpleAttributeSchema attributeSchema) {
                            return getSimple(attributeSchema);
                        }

                        @Override
                        public Serializable visitNested(NestedAttributeSchema attributeSchema) {
                            return new AttributeStore(attributeSchema, getNested(attributeSchema)).toIdMap(filter);
                        }

                        @Override
                        public Serializable visitIndexed(IndexedAttributeSchema attributeSchema) {
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
                .collect(PairUtils.toMap())
        );
    }

    public void updateValues(Map<Long, Serializable> data) {
        AttributeStore dataStore = new AttributeStore(attributeContainer, new HashMap<>(data));
        for (AttributeSchema attribute : attributeContainer.getAttributes()) {
            attribute.accept(new AttributeValueDeterminer(dataStore))
                    .ifPresent(value -> putSimple(attribute, value));
        }
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
        public Optional<Serializable> visitSimple(SimpleAttributeSchema attributeModel) {
            Serializable value = data.getSimple(attributeModel);
            validateRequiredValue("Attribute", attributeModel, () -> value == null);

            return Optional.ofNullable(value);
        }

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
        public Optional<Serializable> visitIndexed(IndexedAttributeSchema attributeSchema) {
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

        private HashMap<Long, Serializable> createNestedInstance(AbstractContainerAttributeSchema nestedModel, HashMap<Long, Serializable> nestedData) {
            HashMap<Long, Serializable> hashMap = new HashMap<>();
            new AttributeStore(nestedModel, hashMap).updateValues(nestedData);
            return hashMap;
        }
    }
}
