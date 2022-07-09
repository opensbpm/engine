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
 *****************************************************************************
 */
package org.opensbpm.engine.core.engine;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.NestedAttributeSchema;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import static java.util.stream.Collectors.toList;

class ObjectSchemaConverter {

    private final FunctionState state;

    public ObjectSchemaConverter(FunctionState state) {
        this.state = Objects.requireNonNull(state, "FunctionState must be non null");
    }

    public FunctionState getState() {
        return state;
    }

    public List<ObjectSchema> createObjectSchemas(ProcessModel processModel) {
        return processModel.getObjectModels().stream()
                .filter(objectModel -> state.hasAnyStatePermission(objectModel))
                .map(new SchemaCreator())
                .collect(toList());
    }

    public ObjectSchema convertToObjectSchema(ObjectModel objectModel) {
        return new SchemaCreator().apply(objectModel);
    }

    private class SchemaCreator implements AttributeModelVisitor<AttributeSchema>, Function<ObjectModel, ObjectSchema> {

        @Override
        public AttributeSchema visitSimple(SimpleAttributeModel simpleAttribute) {
            AttributeSchema attributeSchema = new AttributeSchema(simpleAttribute.getId(), simpleAttribute.getName(), simpleAttribute.getFieldType());
            attributeSchema.setRequired(state.isMandatory(simpleAttribute));
            attributeSchema.setReadonly(state.hasReadPermission(simpleAttribute));
            attributeSchema.setIndexed(simpleAttribute.isIndexed());

            //PENDING add attributeSchema.getDefaultValue()
            return attributeSchema;
        }

        @Override
        public AttributeSchema visitReference(ReferenceAttributeModel referenceAttribute) {
            AttributeSchema attributeSchema = new AttributeSchema(referenceAttribute.getId(), referenceAttribute.getName(), FieldType.REFERENCE);
            attributeSchema.setRequired(state.isMandatory(referenceAttribute));
            attributeSchema.setReadonly(state.hasReadPermission(referenceAttribute));

            attributeSchema.setAutocompleteReference(convertToObjectSchema(referenceAttribute.getReference()));

            //PENDING add attributeSchema.getDefaultValue()
            return attributeSchema;
        }

        @Override
        public AttributeSchema visitNested(NestedAttributeModel nestedAttribute) {
            List<AttributeSchema> attributes = createAttributes(nestedAttribute.getAttributeModels());
            return new NestedAttributeSchema(nestedAttribute.getId(), nestedAttribute.getName(), nestedAttribute.getOccurs(), attributes);
        }

        @Override
        public AttributeSchema visitIndexed(IndexedAttributeModel indexedAttribute) {
            List<AttributeSchema> attributes = createAttributes(indexedAttribute.getAttributeModels());
            return new NestedAttributeSchema(indexedAttribute.getId(), indexedAttribute.getName(), indexedAttribute.getOccurs(), attributes);
        }

        @Override
        public ObjectSchema apply(ObjectModel objectModel) {
            List<AttributeSchema> attributes = createAttributes(objectModel.getAttributeModels());
            return ObjectSchema.of(objectModel.getId(), objectModel.getName(), attributes);
        }

        private List<AttributeSchema> createAttributes(Collection<AttributeModel> attributeModels) {
            return attributeModels.stream()
                    .filter(attributeModel -> state.hasAnyPermission(attributeModel))
                    .map(attributeModel -> attributeModel.accept(this))
                    .collect(toList());
        }

        ObjectSchema convertToObjectSchema(ObjectModel objectModel) {
            List<AttributeSchema> attributes = createAttributes(objectModel.getAttributeModels());
            return ObjectSchema.of(objectModel.getId(), objectModel.getName(), attributes);
        }

    }

}