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
import java.util.Optional;
import java.util.function.Function;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.IndexedAttributeSchema;
import org.opensbpm.engine.api.instance.NestedAttributeSchema;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.SimpleAttributeSchema;
import org.opensbpm.engine.core.engine.ScriptExecutorService.BindingContext;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.core.model.entities.State;
import static java.util.stream.Collectors.toList;
import static org.opensbpm.engine.core.model.entities.StateVisitor.functionState;

class ObjectSchemaConverter {

    public static ObjectSchema toObjectSchema(ScriptExecutorService scriptExecutorService, State state, ObjectModel objectModel, BindingContext bindingContext) {
        ObjectSchema objectSchema = new ObjectSchemaConverter(scriptExecutorService, state, bindingContext)
                .convertToObjectSchema(objectModel);
        return objectSchema;
    }

    private final ScriptExecutorService scriptService;
    private final State state;
    private final BindingContext bindingContext;

    public ObjectSchemaConverter(ScriptExecutorService scriptService, State state, BindingContext bindingContext) {
        this.scriptService = Objects.requireNonNull(scriptService, "ScriptExecutorService must be non null");
        this.state = Objects.requireNonNull(state, "State must be non null");
        this.bindingContext = Objects.requireNonNull(bindingContext, "BindingContext must be non null");
    }

    private Optional<FunctionState> getState() {
        return state.accept(functionState());
    }

    public List<ObjectSchema> createObjectSchemas(ProcessModel processModel) {
        return processModel.getObjectModels().stream()
                .filter(objectModel -> hasAnyStatePermission(objectModel))
                .map(new SchemaCreator(state, bindingContext))
                .collect(toList());
    }

    private boolean hasAnyStatePermission(ObjectModel objectModel) {
        return getState()
                .map(functionState -> functionState.hasAnyStatePermission(objectModel))
                .orElse(Boolean.TRUE);
    }

    private ObjectSchema convertToObjectSchema(ObjectModel objectModel) {
        return new SchemaCreator(state, bindingContext).apply(objectModel);
    }

    private class SchemaCreator implements Function<ObjectModel, ObjectSchema> {

        private final State state;
        private final BindingContext bindingContext;

        private SchemaCreator(State state, BindingContext bindingContext) {
            this.state = Objects.requireNonNull(state, "State must be non null");
            this.bindingContext = Objects.requireNonNull(bindingContext, "BindingContext must be non null");
        }

        @Override
        public ObjectSchema apply(ObjectModel objectModel) {
            return toObjectSchema(objectModel);
        }

        private ObjectSchema toObjectSchema(ObjectModel objectModel) {
            List<AttributeSchema> attributes = createAttributes(objectModel.getAttributeModels());
            return ObjectSchema.of(objectModel.getId(), objectModel.getName(), attributes);
        }

        private List<AttributeSchema> createAttributes(Collection<AttributeModel> attributeModels) {
            return attributeModels.stream()
                    .filter(attributeModel -> hasAnyPermission(attributeModel))
                    .map(attributeModel -> attributeModel.accept(new AttributeModelVisitor<AttributeSchema>() {
                @Override
                public AttributeSchema visitSimple(SimpleAttributeModel simpleAttribute) {
                    SimpleAttributeSchema attributeSchema = SimpleAttributeSchema.of(simpleAttribute.getId(), simpleAttribute.getName(), simpleAttribute.getFieldType());
                    getState().ifPresent(functionState -> {
                        attributeSchema.setRequired(functionState.isMandatory(simpleAttribute));
                        attributeSchema.setReadonly(functionState.hasReadPermission(simpleAttribute));
                        functionState.findStatePermission(simpleAttribute)
                                .map(statePermission -> scriptService.evaluateDefaultValueScript(statePermission, bindingContext))
                                .ifPresent(defaultValue -> {
                                    attributeSchema.setDefaultValue(defaultValue);
                                });
                    });
                    attributeSchema.setIndexed(simpleAttribute.isIndexed());

                    return attributeSchema;
                }

                @Override
                public AttributeSchema visitReference(ReferenceAttributeModel referenceAttribute) {
                    SimpleAttributeSchema attributeSchema = SimpleAttributeSchema.ofReference(
                            referenceAttribute.getId(),
                            referenceAttribute.getName(),
                            toObjectSchema(referenceAttribute.getReference())
                    );
                    getState().ifPresent(functionState -> {
                        attributeSchema.setRequired(functionState.isMandatory(referenceAttribute));
                        attributeSchema.setReadonly(functionState.hasReadPermission(referenceAttribute));
                    });

                    //PENDING add attributeSchema.getDefaultValue()
                    return attributeSchema;
                }

//                @Override
//                public AttributeSchema visitReference(ReferenceAttributeModel referenceAttribute) {
//                    List<AttributeSchema> attributes = createAttributes(referenceAttribute.getAttributeModels());
//                    ReferenceAttributeSchema attributeSchema = ReferenceAttributeSchema.create(
//                            referenceAttribute.getId(), 
//                            referenceAttribute.getName(), 
//                            attributes
//                    );
//                    attributeSchema.setAutocompleteReference(toObjectSchema(referenceAttribute.getReference()));
//                    getState().ifPresent(functionState -> {
//                        attributeSchema.setRequired(functionState.isMandatory(referenceAttribute));
//                        attributeSchema.setReadonly(functionState.hasReadPermission(referenceAttribute));
//                    });
//
//                    //PENDING add attributeSchema.getDefaultValue()
//                    return attributeSchema;
//                }
                @Override
                public AttributeSchema visitNested(NestedAttributeModel nestedAttribute) {
                    List<AttributeSchema> attributes = createAttributes(nestedAttribute.getAttributeModels());
                    return NestedAttributeSchema.createNested(nestedAttribute.getId(), nestedAttribute.getName(), attributes);
                }

                @Override
                public AttributeSchema visitIndexed(IndexedAttributeModel indexedAttribute) {
                    List<AttributeSchema> attributes = createAttributes(indexedAttribute.getAttributeModels());
                    return IndexedAttributeSchema.create(indexedAttribute.getId(), indexedAttribute.getName(), attributes);
                }

            }))
                    .collect(toList());
        }

        private boolean hasAnyPermission(AttributeModel attributeModel) {
            return getState()
                    .map(functionState -> functionState.hasAnyPermission(attributeModel))
                    .orElse(Boolean.TRUE);
        }

        private Optional<FunctionState> getState() {
            return state.accept(functionState());
        }

    }

}
