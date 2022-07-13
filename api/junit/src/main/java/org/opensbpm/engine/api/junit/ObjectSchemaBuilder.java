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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.IndexedAttributeSchema;
import org.opensbpm.engine.api.instance.NestedAttributeSchema;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.model.FieldType;

public class ObjectSchemaBuilder {

    public static ObjectSchemaBuilder schema(String name) {
        return new ObjectSchemaBuilder(name);
    }

    public static SimpleAttributeBuilder simple(String name, FieldType type) {
        return new SimpleAttributeBuilder(name, type);
    }

    public static NestedAttributeBuilder nested(String name) {
        return new NestedAttributeBuilder(name);
    }

    public static IndexedAttributeBuilder indexed(String name) {
        return new IndexedAttributeBuilder(name);
    }

    private final String name;

    private final AtomicLong id = new AtomicLong(0);
    private final List<AttributeBuilder> attributeBuilders = new ArrayList<>();

    private ObjectSchemaBuilder(String name) {
        this.name = Objects.requireNonNull(name, "name must be non null");
    }

    public ObjectSchemaBuilder attribute(AttributeBuilder attributeBuilder) {
        attributeBuilders.add(attributeBuilder);
        return this;
    }

    public ObjectSchema build() {
        List<AttributeSchema> attributes = attributeBuilders.stream()
                .map(builder -> builder.build(id))
                .collect(Collectors.toList());

        return ObjectSchema.of(id.incrementAndGet(), name, attributes);
    }

    public static abstract class AttributeBuilder<T extends AttributeSchema, V extends AttributeBuilder<T, V>> {

        protected String name;
        protected boolean required = true;

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

    public static class SimpleAttributeBuilder extends AttributeBuilder</*Simple*/AttributeSchema, SimpleAttributeBuilder> {

        private final FieldType type;

        public SimpleAttributeBuilder(String name, FieldType type) {
            super(name);
            this.type = type;
        }

        @Override
        protected SimpleAttributeBuilder self() {
            return this;
        }

        @Override
        public /*Simple*/ AttributeSchema build(AtomicLong id) {
            AttributeSchema attributeSchema = new /*Simple*/ AttributeSchema(id.getAndIncrement(), name, type);
            attributeSchema.setRequired(required);
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
