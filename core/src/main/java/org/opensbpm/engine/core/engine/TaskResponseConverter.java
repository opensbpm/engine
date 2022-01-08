package org.opensbpm.engine.core.engine;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.script.ScriptEngine;

import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.NestedAttributeSchema;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;

import static org.opensbpm.engine.utils.StreamUtils.mapToList;

class TaskResponseConverter {

    private ScriptEngine scriptEngine;
    private FunctionState state;

    public TaskResponseConverter(ScriptEngine scriptEngine, FunctionState state) {
        this.scriptEngine = scriptEngine;
        this.state = state;
    }

    public TaskResponse convert(Subject subject, List<NextState> nextStates) {
        ProcessInstance processInstance = subject.getProcessInstance();
        List<ObjectSchema> objectSchemas = createObjectSchemas(processInstance.getProcessModel());
        List<ObjectData> datas = createObjectDatas(processInstance);

        return TaskResponse.of(subject.getId(), nextStates, subject.getLastChanged(), objectSchemas, datas);
    }

    private List<ObjectSchema> createObjectSchemas(ProcessModel processModel) {
        return processModel.getObjectModels().stream()
                .filter(objectModel -> state.hasAnyStatePermission(objectModel))
                .map(new SchemaCreator())
                .collect(toList());
    }

    private List<ObjectData> createObjectDatas(ProcessInstance processInstance) {
        return mapToList(processInstance.getObjectInstances(),
                objectInstance -> createObjectData(objectInstance));
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

    private ObjectData createObjectData(ObjectInstance objectInstance) {
        ObjectModel objectModel = objectInstance.getObjectModel();
        AttributeStore attributeStore = objectInstance.getAttributeStore();

        return new ObjectDataCreator(scriptEngine).createObjectData(objectModel, state, attributeStore);
    }

}
