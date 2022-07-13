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
        return SimpleAttributeBuilder.create(name, type);
    }

    public static NestedAttributeBuilder nested(String name) {
        return NestedAttributeBuilder.create(name);
    }

    public static IndexedAttributeBuilder indexed(String name) {
        return IndexedAttributeBuilder.create(name);
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

    public static abstract class AttributeBuilder<T extends AttributeSchema> {

        abstract T build(AtomicLong id);
    }

    public static class SimpleAttributeBuilder extends AttributeBuilder</*Simple*/AttributeSchema> {

        public static SimpleAttributeBuilder create(String name, FieldType type) {
            return new SimpleAttributeBuilder(name, type);
        }

        private String name;
        private FieldType type;
        boolean required = true;

        public SimpleAttributeBuilder(String name, FieldType type) {
            this.name = name;
            this.type = type;
        }

        public SimpleAttributeBuilder required() {
            required = true;
            return this;
        }

        public /*Simple*/ AttributeSchema build(AtomicLong id) {
            AttributeSchema attributeSchema = new /*Simple*/ AttributeSchema(id.getAndIncrement(), name, type);
            attributeSchema.setRequired(required);
            return attributeSchema;
        }

    }

    public static abstract class ContainerAttributeBuilder<T extends AttributeSchema> extends AttributeBuilder<T> {

        protected final String name;
        protected final FieldType type;
        private final List<AttributeBuilder> attributeBuilders = new ArrayList<>();
        boolean required = true;

        protected ContainerAttributeBuilder(String name, FieldType type) {
            this.name = name;
            this.type = type;
        }

        public ContainerAttributeBuilder required() {
            required = true;
            return this;
        }

        public ContainerAttributeBuilder attribute(AttributeBuilder attributeBuilder) {
            attributeBuilders.add(attributeBuilder);
            return this;
        }
    }

    public static class NestedAttributeBuilder extends ContainerAttributeBuilder<NestedAttributeSchema> {

        public static NestedAttributeBuilder create(String name) {
            return new NestedAttributeBuilder(name);
        }

        private final List<AttributeBuilder> attributeBuilders = new ArrayList<>();
        boolean required = true;

        public NestedAttributeBuilder(String name) {
            super(name, FieldType.NESTED);
        }

        public NestedAttributeBuilder required() {
            required = true;
            return this;
        }

        public NestedAttributeBuilder attribute(AttributeBuilder attributeBuilder) {
            attributeBuilders.add(attributeBuilder);
            return this;
        }

        public NestedAttributeSchema build(AtomicLong id) {
            List<AttributeSchema> attributes = attributeBuilders.stream()
                    .map(builder -> builder.build(id))
                    .collect(Collectors.toList());

            return NestedAttributeSchema.createNested(id.incrementAndGet(), name, attributes);
        }
    }

    public static class IndexedAttributeBuilder extends ContainerAttributeBuilder<IndexedAttributeSchema> {

        public static IndexedAttributeBuilder create(String name) {
            return new IndexedAttributeBuilder(name);
        }

        private final List<AttributeBuilder> attributeBuilders = new ArrayList<>();
        boolean required = true;

        public IndexedAttributeBuilder(String name) {
            super(name, FieldType.LIST);
        }

        public IndexedAttributeBuilder required() {
            required = true;
            return this;
        }

        public IndexedAttributeBuilder attribute(AttributeBuilder attributeBuilder) {
            attributeBuilders.add(attributeBuilder);
            return this;
        }

        public IndexedAttributeSchema build(AtomicLong id) {
            List<AttributeSchema> attributes = attributeBuilders.stream()
                    .map(builder -> builder.build(id))
                    .collect(Collectors.toList());

            return IndexedAttributeSchema.create(id.incrementAndGet(), name, attributes);
        }
    }
}
