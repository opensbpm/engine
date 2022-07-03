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
package org.opensbpm.engine.api.model.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.FieldDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.NestedAttribute;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ReferenceDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToManyDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToOneDefinition;
import static org.opensbpm.engine.utils.StreamUtils.mapToList;

public class ObjectBuilder extends AbstractBuilder<ObjectDefinition,ObjectBuilder> 
        implements HasChildAttributes<ObjectBuilder> {

    private final String name;
    private String displayName;
    private final Map<String, AttributeBuilder<?,?>> attributeBuilders = new LinkedHashMap<>();

    public ObjectBuilder(String name) {
        this.name = name;
    }

    @Override
    protected ObjectBuilder self() {
        return this;
    }
    

    @Override
    public String getName() {
        return name;
    }

    public ObjectBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public ObjectBuilder addAttribute(AttributeBuilder<?,?> attributeBuilder) {
        checkBuilt();
        attributeBuilders.put(attributeBuilder.getName(), attributeBuilder);
        return this;
    }

    @Override
    public AttributeBuilder<?,?> getAttribute(String name) {
        if (!attributeBuilders.containsKey(name)) {
            throw new IllegalArgumentException("Attribute '" + name + "' not found");
        }
        return attributeBuilders.get(name);
    }

    @Override
    protected ObjectDefinition create() {
        List<AttributeDefinition> attributes = mapToList(attributeBuilders.values(), AttributeBuilder::build);
        return new ObjectDefinition() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public List<AttributeDefinition> getAttributes() {
                return attributes;
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append("name", name)
                        .append("displayName", displayName)
                        .append("attributes", attributes)
                        .toString();
            }
        };
    }

    public abstract static class AttributeBuilder<V extends AttributeDefinition, T extends AttributeBuilder<V,T>> 
            extends AbstractBuilder<V,T> {

        public abstract String getName();
    }

    public static class FieldBuilder extends AttributeBuilder<FieldDefinition,FieldBuilder> {

        private final FieldDefinition fieldDefinition;
        private boolean indexed;
        private ObjectBuilder autocompleteObject;

        public FieldBuilder(String name, FieldType fieldType) {
            this.fieldDefinition = new FieldDefinition() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public FieldType getFieldType() {
                    return fieldType;
                }

                @Override
                public boolean isIndexed() {
                    return indexed;
                }

                @Override
                public ObjectDefinition getAucompleteObject() {
                    return autocompleteObject == null ? null : autocompleteObject.build();
                }

                @Override
                public String toString() {
                    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                            .append("name", name)
                            .append("fieldType", fieldType)
                            .append("indexed", indexed)
                            .toString();
                }
            };
        }

        @Override
        protected FieldBuilder self() {
            return this;
        }

        
        @Override
        public String getName() {
            return fieldDefinition.getName();
        }

        public FieldBuilder asIndexed() {
            return withIndexed(true);
        }

        public FieldBuilder withIndexed(boolean indexed) {
            this.indexed = indexed;
            return this;
        }

        public FieldBuilder withAutocompleteObject(ObjectBuilder objectBuilder) {
            this.autocompleteObject = objectBuilder;
            return this;
        }

        @Override
        protected FieldDefinition create() {
            return fieldDefinition;
        }
    }

    public static class ReferenceBuilder extends AttributeBuilder<ReferenceDefinition,ReferenceBuilder> {

        private final ReferenceDefinition referenceDefinition;

        public ReferenceBuilder(String name, ObjectBuilder objectBuilder) {
            this.referenceDefinition = new ReferenceDefinition() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public ObjectDefinition getObjectDefinition() {
                    return objectBuilder.build();
                }

                @Override
                public String toString() {
                    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                            .append("name", name)
                            .append("reference", objectBuilder.getName())
                            .toString();
                }
            };
        }

        @Override
        protected ReferenceBuilder self() {
            return this;
        }
        

        @Override
        public String getName() {
            return referenceDefinition.getName();
        }

        @Override
        protected ReferenceDefinition create() {
            return referenceDefinition;
        }
    }

    public abstract static class AbstractNestedBuilder<T extends AbstractNestedBuilder<T, V>, V extends NestedAttribute> 
            extends AttributeBuilder<V, T> implements HasChildAttributes<T> {

        private final String name;
        private final Map<String, AttributeBuilder<?,?>> attributeBuilders = new LinkedHashMap<>();

        protected AbstractNestedBuilder(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public T addAttribute(AttributeBuilder<?,?> attributeBuilder) {
            checkBuilt();
            attributeBuilders.put(attributeBuilder.getName(), attributeBuilder);
            return self();
        }

        @Override
        public AttributeBuilder<?,?> getAttribute(String name) {
            if (!attributeBuilders.containsKey(name)) {
                throw new IllegalArgumentException("Attribute '" + name + "' not found");
            }
            return attributeBuilders.get(name);
        }

        @Override
        protected V create() {
            List<AttributeDefinition> attributes = mapToList(attributeBuilders.values(), AttributeBuilder::build);
            return create(name, attributes);
        }

        protected abstract V create(String name, List<AttributeDefinition> attributes);

    }

    public static class ToOneBuilder extends AbstractNestedBuilder<ToOneBuilder, ToOneDefinition> {

        public ToOneBuilder(String name) {
            super(name);
        }

        @Override
        protected ToOneBuilder self() {
            return this;
        }
        

        @Override
        protected ToOneDefinition create(String name, List<AttributeDefinition> attributes) {
            return new ToOneDefinition() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public List<AttributeDefinition> getAttributes() {
                    return attributes;
                }

            };
        }

    }

    public static class ToManyBuilder extends AbstractNestedBuilder<ToManyBuilder, ToManyDefinition> {

        public ToManyBuilder(String name) {
            super(name);
        }

        @Override
        protected ToManyBuilder self() {
            return this;
        }

        
        @Override
        protected ToManyDefinition create(String name, List<AttributeDefinition> attributes) {
            return new ToManyDefinition() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public List<AttributeDefinition> getAttributes() {
                    return attributes;
                }
            };
        }

    }
}
