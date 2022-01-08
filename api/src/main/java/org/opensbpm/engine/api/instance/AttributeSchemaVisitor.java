package org.opensbpm.engine.api.instance;

import java.util.Optional;

public interface AttributeSchemaVisitor<T> {
    //TODO: rename classes SimpleAttributeSchema and IndexedNestedAttributeSchema

    T visitSimple(/*Simple*/AttributeSchema attributeSchema);

    T visitNested(NestedAttributeSchema attributeSchema);

    T visitIndexed(/*Indexed*/NestedAttributeSchema attributeSchema);

    static AttributeSchemaVisitor<Optional</*Simple*/AttributeSchema>> simple() {
        return new OptionalAttributeSchemaAdapter</*Simple*/AttributeSchema>() {
            @Override
            public Optional</*Simple*/AttributeSchema> visitSimple(/*Simple*/AttributeSchema simpleSchema) {
                return Optional.of(simpleSchema);
            }
        };
    }

//    static AttributeSchemaVisitor<Optional<ReferenceAttributeSchema>> reference() {
//        return new OptionalAttributeSchemaAdapter<ReferenceAttributeSchema>() {
//            @Override
//            public Optional<ReferenceAttributeSchema> visitReference(ReferenceAttributeSchema referenceSchema) {
//                return Optional.of(referenceSchema);
//            }
//        };
//    }
    static AttributeSchemaVisitor<Optional<NestedAttributeSchema>> nested() {
        return new OptionalAttributeSchemaAdapter<NestedAttributeSchema>() {
            @Override
            public Optional<NestedAttributeSchema> visitNested(NestedAttributeSchema nestedSchema) {
                return Optional.of(nestedSchema);
            }
        };
    }

    static AttributeSchemaVisitor<Optional</*Indexed*/NestedAttributeSchema>> indexed() {
        return new OptionalAttributeSchemaAdapter</*Indexed*/NestedAttributeSchema>() {
            @Override
            public Optional</*Indexed*/NestedAttributeSchema> visitIndexed(/*Indexed*/NestedAttributeSchema indexedSchema) {
                return Optional.of(indexedSchema);
            }
        };
    }

    public static class OptionalAttributeSchemaAdapter<T> implements AttributeSchemaVisitor<Optional<T>> {

        @Override
        public Optional<T> visitSimple(/*Simple*/AttributeSchema simpleSchema) {
            return Optional.empty();
        }

//        @Override
//        public Optional<T> visitReference(ReferenceAttributeSchema referenceSchema) {
//            return Optional.empty();
//        }
        @Override
        public Optional<T> visitNested(NestedAttributeSchema nestedSchema) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitIndexed(/*Indexed*/NestedAttributeSchema indexedSchema) {
            return Optional.empty();
        }
    }
}
