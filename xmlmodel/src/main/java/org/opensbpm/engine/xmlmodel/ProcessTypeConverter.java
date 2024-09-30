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
package org.opensbpm.engine.xmlmodel;

import org.opensbpm.engine.api.model.FieldType;

import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.object;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.permission;

import org.opensbpm.engine.api.model.builder.ProcessBuilder;
import org.opensbpm.engine.api.model.builder.SubjectBuilder;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;

import static org.opensbpm.engine.api.model.builder.DefinitionFactory.process;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.field;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.receiveState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.sendState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.serviceSubject;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.simplePermission;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toMany;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.toOne;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

import org.opensbpm.engine.api.model.builder.FunctionStateBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.AbstractAttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.PermissionBuilder;
import org.opensbpm.engine.api.model.builder.HasChildAttributes;
import org.opensbpm.engine.api.model.builder.ObjectBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AttributeBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AbstractNestedBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToManyBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToOneBuilder;
import org.opensbpm.engine.api.model.builder.ReceiveStateBuilder;
import org.opensbpm.engine.api.model.builder.SendStateBuilder;
import org.opensbpm.engine.api.model.builder.StateBuilder;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import org.opensbpm.engine.utils.PairUtils;
import org.opensbpm.engine.xmlmodel.processmodel.AttributePermissionType;
import org.opensbpm.engine.xmlmodel.processmodel.Field;
import org.opensbpm.engine.xmlmodel.processmodel.FunctionStateType;
import org.opensbpm.engine.xmlmodel.processmodel.ObjectPermissionType;
import org.opensbpm.engine.xmlmodel.processmodel.ObjectType;
import org.opensbpm.engine.xmlmodel.processmodel.ProcessModelState;
import org.opensbpm.engine.xmlmodel.processmodel.ProcessType;
import org.opensbpm.engine.xmlmodel.processmodel.ReceiveStateType;
import org.opensbpm.engine.xmlmodel.processmodel.ReceiveTransitionType;
import org.opensbpm.engine.xmlmodel.processmodel.SendStateType;
import org.opensbpm.engine.xmlmodel.processmodel.ServiceSubject;
import org.opensbpm.engine.xmlmodel.processmodel.StateEventType;
import org.opensbpm.engine.xmlmodel.processmodel.StateType;
import org.opensbpm.engine.xmlmodel.processmodel.SubjectType;
import org.opensbpm.engine.xmlmodel.processmodel.ToManyType;
import org.opensbpm.engine.xmlmodel.processmodel.ToOneType;
import org.opensbpm.engine.xmlmodel.processmodel.UserSubject;
import org.w3c.dom.Element;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.nestedPermission;

public class ProcessTypeConverter {

    public ProcessDefinition convert(ProcessType processType) {
        ProcessBuilder processBuilder = process(processType.getName())
                .description(processType.getDescription())
                .version(processType.getVersion());
        if (ProcessModelState.INACTIVE == processType.getState()) {
            processBuilder.asIncative();
        }

        Map<ObjectBuilder, ObjectType> objectsCache = processType.getObject().stream()
                .map(objectType -> {
                    ObjectBuilder objectBuilder = object(objectType.getName());
                    objectBuilder.withDisplayName(objectType.getDisplayName());
                    processBuilder.addObject(objectBuilder);

                    return Pair.of(objectBuilder, objectType);
                })
                .collect(PairUtils.toMap());

        for (Map.Entry<ObjectBuilder, ObjectType> entry : objectsCache.entrySet()) {
            for (AttributeBuilder<?, ?> attributeBuilder : createAttributes(processBuilder, entry.getValue().getFieldOrToOneOrToMany())) {
                entry.getKey().addAttribute(attributeBuilder);
            }
        }

        for (SubjectType subjectType : processType.getUserSubjectOrServiceSubject()) {
            processBuilder.addSubject(createSubject(subjectType));
        }

        for (SubjectType subjectType : processType.getUserSubjectOrServiceSubject()) {
            updateSubject(processBuilder, subjectType);
        }

        return processBuilder.build();
    }

    private void updateSubject(ProcessBuilder processBuilder, SubjectType subjectType) {
        SubjectBuilder<?, ? extends SubjectDefinition> subjectBuilder;
        subjectBuilder = processBuilder.getSubject(subjectType.getName());

        for (StateType stateType : subjectType.getFunctionStateOrSendStateOrReceiveState()) {
            subjectBuilder.addState(createState(stateType, processBuilder));
        }

        updateHeads(subjectType, subjectBuilder, processBuilder);
    }

    private List<AttributeBuilder<?, ?>> createAttributes(ProcessBuilder processBuilder, List<Object> attributeTypes) {
        List<AttributeBuilder<?, ?>> attributeBuilders = new ArrayList<>();
        for (Object attributeType : attributeTypes) {
            AttributeBuilder<?, ?> attributeBuilder;
            if (attributeType instanceof Field) {
                Field field = (Field) attributeType;
                FieldBuilder fieldBuilder = field(field.getValue(), FieldType.valueOf(field.getType().value()));
                Optional.ofNullable(field.isIndexed())
                        .ifPresent(indexed -> fieldBuilder.withIndexed(indexed));
                Optional.ofNullable(field.getAutocomplete())
                        .ifPresent(autocomplete
                                -> fieldBuilder.withAutocompleteObject(processBuilder.getObject(autocomplete)));
                attributeBuilder = fieldBuilder;
            } else if (attributeType instanceof ToOneType) {
                ToOneType toOneType = (ToOneType) attributeType;
                ToOneBuilder toOneBuilder = toOne(toOneType.getName());
                for (AttributeBuilder<?, ?> childBuilder : createAttributes(processBuilder, toOneType.getFieldOrToOneOrToMany())) {
                    toOneBuilder.addAttribute(childBuilder);
                }
                attributeBuilder = toOneBuilder;
            } else if (attributeType instanceof ToManyType) {
                ToManyType toManyType = (ToManyType) attributeType;
                ToManyBuilder toManyBuilder = toMany(toManyType.getName());
                for (AttributeBuilder<?, ?> childBuilder : createAttributes(processBuilder, toManyType.getFieldOrToOneOrToMany())) {
                    toManyBuilder.addAttribute(childBuilder);
                }
                attributeBuilder = toManyBuilder;
            } else {
                throw new UnsupportedOperationException("AttributeType " + attributeType + " not supported yet");
            }
            attributeBuilders.add(attributeBuilder);
        }
        return attributeBuilders;
    }

    private SubjectBuilder<?, ?> createSubject(SubjectType subjectType) {
        SubjectBuilder<?, ?> subjectBuilder;
        if (subjectType instanceof UserSubject) {
            subjectBuilder = userSubject(subjectType.getName(), ((UserSubject) subjectType).getRoles());
        } else if (subjectType instanceof ServiceSubject) {
            subjectBuilder = serviceSubject(subjectType.getName());
        } else {
            throw new UnsupportedOperationException("SubjectType " + subjectType + " not supported yet");
        }
        if (subjectType.isStarter() != null && subjectType.isStarter()) {
            subjectBuilder.asStarter();
        }
        return subjectBuilder;
    }

    private StateBuilder<?, ?> createState(StateType stateType, ProcessBuilder processBuilder) {
        StateBuilder<?, ?> stateBuilder;
        if (stateType instanceof FunctionStateType) {
            FunctionStateType functionStateType = (FunctionStateType) stateType;
            FunctionStateBuilder functionState = functionState(stateType.getName());

            Optional.ofNullable(functionStateType.getProvider())
                    .ifPresent(provider -> functionState.withProvider(provider));
            if (functionStateType.getParameters() != null) {
                for (Element element : functionStateType.getParameters().getAny()) {
                    functionState.addParameter(element.getLocalName(), element.getFirstChild().getNodeValue());
                }
            }

            for (ObjectPermissionType objectPermissionType : functionStateType.getObject()) {
                ObjectBuilder objectBuilder = processBuilder.getObject(objectPermissionType.getName());
                PermissionBuilder permissionBuilder = permission(objectBuilder)
                            .addPermissions(createAttributePermissions(objectBuilder, objectPermissionType.getAttributeOrObject()));
                functionState.addPermission(permissionBuilder);
            }
            
            stateBuilder = functionState;
        } else if (stateType instanceof ReceiveStateType) {
            stateBuilder = receiveState(stateType.getName());
        } else if (stateType instanceof SendStateType) {
            SendStateBuilder sendState = sendState(stateType.getName(),
                    processBuilder.getSubject(((SendStateType) stateType).getReceiver()),
                    processBuilder.getObject(((SendStateType) stateType).getMessage())
            );
            if (((SendStateType) stateType).isAsync() != null && ((SendStateType) stateType).isAsync()) {
                sendState.asAsync();
            }
            stateBuilder = sendState;
        } else {
            throw new UnsupportedOperationException("StateType " + stateType + " not supported yet");
        }
        if (StateEventType.START == stateType.getEventType()) {
            stateBuilder.asStart();
        } else if (StateEventType.END == stateType.getEventType()) {
            stateBuilder.asEnd();
        }
        return stateBuilder;
    }

    private void updateHeads(SubjectType subjectType, SubjectBuilder<?, ?> subjectBuilder, ProcessBuilder processBuilder) {
        for (StateType stateType : subjectType.getFunctionStateOrSendStateOrReceiveState()) {
            if (stateType instanceof FunctionStateType) {
                FunctionStateBuilder functionStateBuilder = (FunctionStateBuilder) subjectBuilder.getState(stateType.getName());
                for (String toState : ((FunctionStateType) stateType).getToState()) {
                    StateBuilder<?, ?> toStateBuilder = subjectBuilder.getState(toState);
                    functionStateBuilder.toHead(toStateBuilder);
                }
            } else if (stateType instanceof ReceiveStateType) {
                ReceiveStateBuilder receiveStateBuilder = (ReceiveStateBuilder) subjectBuilder.getState(stateType.getName());
                for (ReceiveTransitionType transitionType : ((ReceiveStateType) stateType).getMessage()) {
                    ObjectBuilder objectBuilder = processBuilder.getObject(transitionType.getObject());
                    StateBuilder<?, ?> toStateBuilder = subjectBuilder.getState(transitionType.getToState());

                    receiveStateBuilder.toHead(objectBuilder, toStateBuilder);
                }
            } else if (stateType instanceof SendStateType) {
                SendStateBuilder sendStateBuilder = (SendStateBuilder) subjectBuilder.getState(stateType.getName());

                Optional.ofNullable(((SendStateType) stateType).getToState())
                        .map(stateName -> subjectBuilder.getState(stateName))
                        .ifPresent(toStateBuilder -> sendStateBuilder.toHead(toStateBuilder));

            } else {
                throw new UnsupportedOperationException("StateType " + stateType + " not supported yet");
            }

        }
    }

    private List<AbstractAttributePermissionBuilder<?, ?>> createAttributePermissions(HasChildAttributes<?> parentAttributeBuilder, List attributeTypes) {
        List<AbstractAttributePermissionBuilder<?, ?>> permissions = new ArrayList<>();
        for (Object abstractAttributeType : attributeTypes) {
            AbstractAttributePermissionBuilder<?, ?> permissionBuilder;
            if (abstractAttributeType instanceof AttributePermissionType) {
                AttributePermissionType attributeType = (AttributePermissionType) abstractAttributeType;

                AttributeBuilder<? extends AttributeDefinition, ?> attributeBuilder = parentAttributeBuilder.getAttribute(attributeType.getName());
                permissionBuilder = simplePermission(attributeBuilder,
                        Permission.valueOf(attributeType.getPermission().value()),
                        attributeType.isMandatory());
                
                Optional.ofNullable(attributeType.getDefaultValue())
                        .ifPresent(permissionBuilder::addDefaultValue);
                
            } else if (abstractAttributeType instanceof ObjectPermissionType) {
                ObjectPermissionType attributeType = (ObjectPermissionType) abstractAttributeType;

                AbstractNestedBuilder<?, ?> attributeBuilder = (AbstractNestedBuilder<?, ?>) parentAttributeBuilder.getAttribute(attributeType.getName());
                permissionBuilder = nestedPermission(attributeBuilder, Permission.WRITE, false)
                    .addPermissions(createAttributePermissions(attributeBuilder, attributeType.getAttributeOrObject()));
            } else {
                throw new UnsupportedOperationException("AttributePermissionType " + abstractAttributeType + " not supported yet");
            }

            //attributeType.
            permissions.add(permissionBuilder);
        }
        return permissions;
    }


}
