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
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public final class SourceMap {

    private final IsAttributesContainer attributesContainer;
    private final Map<String, Object> sourceData;
    private final String id;

    public SourceMap(IsAttributesContainer attributesContainer, Map<String, Object> sourceData, String id) {
        this.attributesContainer = Objects.requireNonNull(attributesContainer, "attributesContainer must be non null");
        this.sourceData = Objects.requireNonNull(sourceData, "sourceData must be non null");
        this.id = id;
    }

    public String getId() {
        return id;
    }

    private AttributeSchema findAttribute(String name) {
        return attributesContainer.getAttributes().stream()
                .filter(attributeSchema -> attributeSchema.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Attribute '" + name + "' not found in object " + attributesContainer.getName()));
    }

    public HashMap<Long, Serializable> toIdMap() {
        return new HashMap<Long, Serializable>(
                sourceData.entrySet().stream().map(entry -> {
                    AttributeSchema attributeSchema = findAttribute(entry.getKey());
                    Serializable value = attributeSchema.accept(new AttributeSchemaVisitor<Serializable>() {
                        @Override
                        public Serializable visitSimple(AttributeSchema attributeSchema) {
                            return (Serializable) entry.getValue();
                        }

                        @Override
                        public Serializable visitNested(NestedAttributeSchema attributeSchema) {
                            return new SourceMap(attributeSchema, (Map<String, Object>) entry.getValue(),null).toIdMap();
                        }

                        @Override
                        public Serializable visitIndexed(IndexedAttributeSchema attributeSchema) {
                            ArrayList<Map<Long, Serializable>> values = new ArrayList<>();
                            for (Map<String, Object> data : (List<Map<String, Object>>) entry.getValue()) {
                                values.add(new SourceMap(attributeSchema, data,null).toIdMap());
                            }
                            return values;
                        }
                    });
                    return Pair.of(attributeSchema.getId(), value);
                }).collect(Collectors.toMap(pair -> pair.getKey(), pair -> pair.getValue()))
        );
    }

}
