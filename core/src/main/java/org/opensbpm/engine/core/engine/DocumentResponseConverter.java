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
package org.opensbpm.engine.core.engine;

/**
 *
 * @author stefan
 */
class DocumentResponseConverter {

//    private final ScriptEngine scriptEngine;
//    private final FunctionState state;
//
//    public DocumentResponseConverter(ScriptEngine scriptEngine, FunctionState state) {
//        this.scriptEngine = Objects.requireNonNull(scriptEngine, "ScriptEngine must be non null");
//        this.state = Objects.requireNonNull(state, "ScriptEngine must be non null");
//    }
//
//    public DocumentResponse convert(ProcessInstance processInstance) {
//        List<ObjectSchema> objectSchemas = createObjectSchemas(processInstance.getProcessModel());
//        List<ObjectData> datas = createData(processInstance);
//        return new DocumentResponse(objectSchemas, datas);
//    }
//
//    private List<ObjectSchema> createObjectSchemas(ProcessModel processModel) {
//        Stream<ObjectModel> objects = processModel.getObjectModels(state);
//        return mapToList(objects, objectModel -> convertToObjectSchema(objectModel));
//    }
//
//    ObjectSchema convertToObjectSchema(ObjectModel objectModel) {
//        List<AttributeSchema> attributes = createAttributes(objectModel.getAttributeModels());
//        return ObjectSchema.of(objectModel.getId(), objectModel.getName(), attributes);
//    }
//
//    private List<AttributeSchema> createAttributes(Collection<AttributeModel> attributeModels) {
//        Stream<AttributeModel> attributes = attributeModels.stream().filter(attributeModel -> state.hasAnyPermission(attributeModel));
//        return mapToList(attributes, attributeModel -> createAttributeSchema(attributeModel));
//    }
//
//    private AttributeSchema createAttributeSchema(AttributeModel attributeModel) {
//        return attributeModel.accept(new AttributeModelVisitor<AttributeSchema>() {
//            @Override
//            public AttributeSchema visitSimple(SimpleAttributeModel simpleAttribute) {
//                AttributeSchema attributeSchema = new AttributeSchema(simpleAttribute.getId(), simpleAttribute.getName(), simpleAttribute.getFieldType());
//                attributeSchema.setRequired(state.isMandatory(simpleAttribute));
//                attributeSchema.setReadonly(state.hasReadPermission(simpleAttribute));
//                attributeSchema.setIndexed(simpleAttribute.isIndexed());
//
//                //TODO add/check permission of autocomplete-reference
//                simpleAttribute.getAutocompleteObjectModel().ifPresent(autocompleteObject -> {
//                    attributeSchema.setAutocompleteReference(convertToObjectSchema(autocompleteObject));
//                });
//                //PENDING add attributeSchema.getDefaultValue()
//                return attributeSchema;
//            }
//
//            @Override
//            public AttributeSchema visitReference(ReferenceAttributeModel referenceAttribute) {
//                //TODO not sure about that code
//                AttributeSchema attributeSchema = new AttributeSchema(referenceAttribute.getId(), referenceAttribute.getName(), FieldType.STRING);
//                attributeSchema.setRequired(state.isMandatory(referenceAttribute));
//                attributeSchema.setReadonly(state.hasReadPermission(referenceAttribute));
//                attributeSchema.setAutocompleteReference(convertToObjectSchema(referenceAttribute.getReference()));
//                return attributeSchema;
//            }
//            
//            @Override
//            public AttributeSchema visitNested(NestedAttributeModel nestedAttribute) {
//                List<AttributeSchema> attributes = createAttributes(nestedAttribute.getAttributeModels());
//                return new NestedAttributeSchema(nestedAttribute.getId(), nestedAttribute.getName(), nestedAttribute.getOccurs(), attributes);
//            }
//
//            @Override
//            public AttributeSchema visitIndexed(IndexedAttributeModel indexedAttribute) {
//                List<AttributeSchema> attributes = createAttributes(indexedAttribute.getAttributeModels());
//                return new NestedAttributeSchema(indexedAttribute.getId(), indexedAttribute.getName(), indexedAttribute.getOccurs(), attributes);
//            }
//        });
//    }
//
//    private List<ObjectData> createData(ProcessInstance processInstance) {
//        return mapToList(processInstance.getObjectInstances(), objectInstance -> toObjectData(objectInstance));
//    }
//
//    private ObjectData toObjectData(ObjectInstance objectInstance) {
//        HashMap<Long, Serializable> data = objectInstance.getAttributeStore().toIdMap(attributeModel -> state.hasAnyPermission(attributeModel));
//        ObjectData objectData = new ObjectData(objectInstance.getObjectModel().getName(), objectInstance.getId(), data);
//        evaluteObjectDisplayName(objectInstance).ifPresent(displayName -> objectData.setDisplayName(displayName));
//        return objectData;
//    }
//
//    private Optional<String> evaluteObjectDisplayName(ObjectInstance objectInstance) {
//        return Optional.ofNullable(objectInstance.getObjectModel().getDisplayName())
//                .filter(displayName -> !displayName.isEmpty())
//                .map(displayName -> evalObjectScript(objectInstance, String.format("\"%s\"", displayName)));
//    }
//
//    private String evalObjectScript(ObjectInstance objectInstance, String script) throws RuntimeException {
//        try {
//            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
//            ObjectBean objectBean = new ObjectBean(objectInstance);
//            for (AttributeModel attributeModel : objectBean.getAttributeModels()) {
//                bindings.put(attributeModel.getName(), objectBean.get(attributeModel.getName()));
//            }
//            //eval returns GString; convert it with toString()
//            return scriptEngine.eval(script, bindings).toString();
//        } catch (ScriptException ex) {
//            Logger.getLogger(EngineConverter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            return ex.getMessage();
//        }
//    }

}
