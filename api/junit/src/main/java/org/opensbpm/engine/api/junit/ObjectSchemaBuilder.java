/** *****************************************************************************
 * Copyright (C) 2022 Stefan Sedelmaier
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
package org.opensbpm.engine.api.junit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.IndexedAttributeSchema;
import org.opensbpm.engine.api.instance.NestedAttributeSchema;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.Options;
import org.opensbpm.engine.api.instance.ReferenceAttributeSchema;
import org.opensbpm.engine.api.instance.SimpleAttributeSchema;
import org.opensbpm.engine.api.model.FieldType;
import static org.opensbpm.engine.utils.StreamUtils.oneOrMoreAsList;

public class ObjectSchemaBuilder {

    /**
     * create a new {@link ObjectSchemaBuilder}
     *
     * @param name name of the resulting {@link ObjectSchema}
     * @return new created {@link ObjectSchemaBuilder} instance
     */
    public static ObjectSchemaBuilder schema(String name) {
        return new ObjectSchemaBuilder(name);
    }

    /**
     * create a new {@link SimpleAttributeBuilder}
     *
     * @param name name of the resulting {@link SimpleAttribute}
     * @param type type of the resulting {@link SimpleAttribute}
     * @return new created {@link SimpleAttributeBuilder} instance
     */
    public static SimpleAttributeBuilder simple(String name, FieldType type) {
        return new SimpleAttributeBuilder(name, type);
    }

    /**
     * create a new {@link SimpleAttributeBuilder}
     *
     * @param name name of the resulting {@link SimpleAttribute}
     * @param type type of the resulting {@link SimpleAttribute}
     * @return new created {@link SimpleAttributeBuilder} instance
     */
    public static SimpleAttributeBuilder simple(String name, FieldType type, Serializable option, Serializable... options) {
        return simple(name, type).withOptions(option, options);
    }

    /**
     * create a new {@link ReferencedAttributeBuilder}
     *
     * @param name name of the resulting {@link SimpleAttribute}
     * @param objectSchema schema of the resulting {@link SimpleAttribute}
     * @return new created {@link ReferencedAttributeBuilder} instance
     */
    public static ReferencedAttributeBuilder referenced(String name, ObjectSchema objectSchema) {
        return new ReferencedAttributeBuilder(name, objectSchema);
    }

    /**
     * create a new {@link NestedAttributeBuilder}
     *
     * @param name name of the resulting {@link NestedAttributeSchema}
     * @return new created {@link NestedAttributeBuilder} instance
     */
    public static NestedAttributeBuilder nested(String name) {
        return new NestedAttributeBuilder(name);
    }

    /**
     * create a new {@link IndexedAttributeBuilder}
     *
     * @param name name of the resulting {@link IndexedAttributeSchema}
     * @return new created {@link IndexedAttributeBuilder} instance
     */
    public static IndexedAttributeBuilder indexed(String name) {
        return new IndexedAttributeBuilder(name);
    }

    private final String name;

    private final AtomicLong id = new AtomicLong(0);
    private final List<AttributeBuilder> attributeBuilders = new ArrayList<>();

    private ObjectSchemaBuilder(String name) {
        this.name = Objects.requireNonNull(name, "name must be non null");
    }

    /**
     * Adds a new {@link AttributeBuilder} to this schema builder.
     *
     * @param attributeBuilder {@link AttributeBuilder} to add.
     * @return a reference to this object.
     */
    public ObjectSchemaBuilder attribute(AttributeBuilder attributeBuilder) {
        attributeBuilders.add(attributeBuilder);
        return this;
    }

    /**
     * Creates the {@link ObjectSchema}. All previous added
     * {@link AttributeBuilder} will be build and the resulting
     * {@link AttributeSchema} are added to resulting schema.
     *
     * @return a new {@link ObjectSchema}
     */
    public ObjectSchema build() {
        List<AttributeSchema> attributes = attributeBuilders.stream()
                .map(builder -> builder.build(id))
                .collect(Collectors.toList());

        return ObjectSchema.of(id.incrementAndGet(), name, attributes);
    }

    public static abstract class AttributeBuilder<T extends AttributeSchema, V extends AttributeBuilder<T, V>> {

        protected String name;
        protected boolean required;

        protected AttributeBuilder(String name) {
            this.name = Objects.requireNonNull(name, "name must be non null");
        }

        protected abstract V self();

        public final V required() {
            required = true;
            return self();
        }

        abstract T build(AtomicLong id);
    }

    public static class SimpleAttributeBuilder extends AttributeBuilder<SimpleAttributeSchema, SimpleAttributeBuilder> {

        private final FieldType type;
        private Options options;

        public SimpleAttributeBuilder(String name, FieldType type) {
            super(name);
            this.type = type;
        }

        @Override
        protected SimpleAttributeBuilder self() {
            return this;
        }

        public SimpleAttributeBuilder withOptions(Serializable option, Serializable... options) {
            this.options = Options.of(oneOrMoreAsList(option, options));
            return self();
        }

        @Override
        public SimpleAttributeSchema build(AtomicLong id) {
            SimpleAttributeSchema attributeSchema = SimpleAttributeSchema.of(id.getAndIncrement(), name, type);
            attributeSchema.setRequired(required);
            if (options != null) {
                attributeSchema.setOptions(options);
            }
            return attributeSchema;
        }

    }

    public static abstract class ContainerAttributeBuilder<T extends AttributeSchema, V extends ContainerAttributeBuilder<T, V>>
            extends AttributeBuilder<T, V> {

        private final List<AttributeBuilder> attributeBuilders = new ArrayList<>();

        protected ContainerAttributeBuilder(String name) {
            super(name);
        }

        public final V attribute(AttributeBuilder attributeBuilder) {
            attributeBuilders.add(attributeBuilder);
            return self();
        }

        protected final List<AttributeSchema> buildAttributes(AtomicLong id) {
            return attributeBuilders.stream()
                    .map(builder -> builder.build(id))
                    .collect(Collectors.toList());
        }
    }

    public static class ReferencedAttributeBuilder extends ContainerAttributeBuilder<ReferenceAttributeSchema, ReferencedAttributeBuilder> {

        private final ObjectSchema autocompleteReference;

        public ReferencedAttributeBuilder(String name, ObjectSchema autocompleteReference) {
            super(name);
            this.autocompleteReference = autocompleteReference;
        }

        @Override
        protected ReferencedAttributeBuilder self() {
            return this;
        }

        @Override
        public ReferenceAttributeSchema build(AtomicLong id) {
            ReferenceAttributeSchema attributeSchema = ReferenceAttributeSchema.create(id.getAndIncrement(), name, autocompleteReference);
            attributeSchema.setRequired(required);
            return attributeSchema;
        }

    }

    public static class NestedAttributeBuilder extends ContainerAttributeBuilder<NestedAttributeSchema, NestedAttributeBuilder> {

        public NestedAttributeBuilder(String name) {
            super(name);
        }

        @Override
        protected NestedAttributeBuilder self() {
            return this;
        }

        @Override
        public NestedAttributeSchema build(AtomicLong id) {
            return NestedAttributeSchema.createNested(id.incrementAndGet(), name, buildAttributes(id));
        }
    }

    public static class IndexedAttributeBuilder extends ContainerAttributeBuilder<IndexedAttributeSchema, IndexedAttributeBuilder> {

        public IndexedAttributeBuilder(String name) {
            super(name);
        }

        @Override
        protected IndexedAttributeBuilder self() {
            return this;
        }

        @Override
        public IndexedAttributeSchema build(AtomicLong id) {
            return IndexedAttributeSchema.create(id.incrementAndGet(), name, buildAttributes(id));
        }

    }
}
