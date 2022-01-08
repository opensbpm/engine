/*******************************************************************************
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
 ******************************************************************************/
package org.opensbpm.engine.core.model.entities;

import java.util.Optional;

public interface AttributeModelVisitor<T> {

    T visitSimple(SimpleAttributeModel simpleModel);

    T visitReference(ReferenceAttributeModel referenceModel);

    T visitNested(NestedAttributeModel nestedModel);

    T visitIndexed(IndexedAttributeModel indexedModel);

    static AttributeModelVisitor<Optional<SimpleAttributeModel>> simple() {
        return new OptionalAttributeModelAdapter<SimpleAttributeModel>() {
            @Override
            public Optional<SimpleAttributeModel> visitSimple(SimpleAttributeModel simpleModel) {
                return Optional.of(simpleModel);
            }
        };
    }

    static AttributeModelVisitor<Optional<ReferenceAttributeModel>> reference() {
        return new OptionalAttributeModelAdapter<ReferenceAttributeModel>() {
            @Override
            public Optional<ReferenceAttributeModel> visitReference(ReferenceAttributeModel referenceModel) {
                return Optional.of(referenceModel);
            }
        };
    }

    static AttributeModelVisitor<Optional<NestedAttributeModel>> nested() {
        return new OptionalAttributeModelAdapter<NestedAttributeModel>() {
            @Override
            public Optional<NestedAttributeModel> visitNested(NestedAttributeModel nestedModel) {
                return Optional.of(nestedModel);
            }
        };
    }

    static AttributeModelVisitor<Optional<IndexedAttributeModel>> indexed() {
        return new OptionalAttributeModelAdapter<IndexedAttributeModel>() {
            @Override
            public Optional<IndexedAttributeModel> visitIndexed(IndexedAttributeModel indexedModel) {
                return Optional.of(indexedModel);
            }
        };
    }

    public static class OptionalAttributeModelAdapter<T> implements AttributeModelVisitor<Optional<T>> {

        @Override
        public Optional<T> visitSimple(SimpleAttributeModel simpleModel) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitReference(ReferenceAttributeModel referenceModel) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitNested(NestedAttributeModel nestedModel) {
            return Optional.empty();
        }

        @Override
        public Optional<T> visitIndexed(IndexedAttributeModel indexedModel) {
            return Optional.empty();
        }
    }
}
