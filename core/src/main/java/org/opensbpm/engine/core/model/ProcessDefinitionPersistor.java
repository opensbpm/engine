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
 *****************************************************************************
 */
package org.opensbpm.engine.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinitionVisitor;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.FieldDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.NestedAttribute;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ReferenceDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToManyDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToOneDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.AttributePermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.NestedPermissionDefinition;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.SendStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinitionVisitor;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.ServiceSubjectDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.UserSubjectDefinition;
import org.opensbpm.engine.core.model.entities.AbstractContainerAttributeModel;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.IsAttributeParent;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.StatePermission;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessDefinitionPersistor {

    private final ProcessModelService processModelService;
    private final RoleService roleService;

    public ProcessDefinitionPersistor(ProcessModelService processModelService,
            RoleService roleService) {
        this.processModelService = processModelService;
        this.roleService = roleService;
    }

    @Transactional
    public ProcessModel saveDefinition(ProcessDefinition definition) {
        Objects.requireNonNull(definition);

        int major = definition.getVersion();
        ModelVersion version = processModelService.findNewestVersion(definition.getName(), major)
                .map(model -> model.getVersion().incrementMinor())
                .orElse(new ModelVersion(major, 0));

        ProcessModel processModel = new DefinitionConverter(version).convert(definition);
        return processModelService.save(processModel);
    }

    private class DefinitionConverter implements Converter<ProcessDefinition, ProcessModel> {

        private final ModelVersion version;

        private ProcessModel processModel;

        private Map<ObjectDefinition, ObjectCache> objects;
        private Map<SubjectDefinition, SubjectModel> subjects;
        private Map<StateDefinition, State> states;

        public DefinitionConverter(ModelVersion version) {
            this.version = version;
        }

        @Override
        public ProcessModel convert(ProcessDefinition definition) {
            processModel = new ProcessModel(definition.getName(), version);
            if (definition.getState() != null) {
                processModel.setState(definition.getState());
            }
            processModel.setDescription(definition.getDescription());

            objects = definition.getObjects().stream()
                    .map(this::createObjectModel)
                    .collect(Pair.toMap());

            for (Map.Entry<ObjectDefinition, ObjectCache> entry : objects.entrySet()) {
                createAttributes(entry.getKey(), entry.getValue().getObjectModel(), entry.getValue());
            }

            subjects = definition.getSubjects().stream()
                    .map(this::createSubjectModel)
                    .collect(Pair.toMap());

            states = createStates();

            subjects.keySet().forEach(subjectDefinition
                    -> createSubjectStateGraph(subjectDefinition));

            return processModel;
        }

        private Pair<ObjectDefinition, ObjectCache> createObjectModel(ObjectDefinition objectDefinition) {
            ObjectModel objectModel = processModel.addObjectModel(objectDefinition.getName());
            objectModel.setDisplayName(objectDefinition.getDisplayName());
            return Pair.of(objectDefinition, new ObjectCache(objectModel));
        }

        private void createAttributes(ObjectDefinition objectDefinition, ObjectModel objectModel, ObjectCache objectCache) {
            objectDefinition.getAttributes().forEach(attributeDefinition -> {
                objectModel.addAttributeModel(createAttribute(objectCache, objectCache.getObjectModel(), attributeDefinition));
            });
        }

        private AttributeModel createAttribute(ObjectCache objectCache, IsAttributeParent attributeParent,
                AttributeDefinition attributeDefinition) {
            AttributeModel attributeModel = attributeDefinition.accept(new AttributeDefinitionVisitor<AttributeModel>() {
                @Override
                public AttributeModel visitField(FieldDefinition fieldDefinition) {
                    SimpleAttributeModel simpleAttributeModel;
                    if (attributeParent instanceof ObjectModel) {
                        simpleAttributeModel = new SimpleAttributeModel((ObjectModel) attributeParent, fieldDefinition.getName(), fieldDefinition.getFieldType());
                    } else if (attributeParent instanceof AttributeModel) {
                        simpleAttributeModel = new SimpleAttributeModel((AttributeModel) attributeParent, fieldDefinition.getName(), fieldDefinition.getFieldType());
                    } else {
                        throw new UnsupportedOperationException(attributeParent + " not supported yet");
                    }
                    simpleAttributeModel.setIndexed(fieldDefinition.isIndexed());
                    return simpleAttributeModel;
                }

                @Override
                public AttributeModel visitReference(ReferenceDefinition referenceDefinition) {
                    ObjectModel reference = objects.get(referenceDefinition.getObjectDefinition()).getObjectModel();
                    ReferenceAttributeModel referenceAttributeModel;
                    if (attributeParent instanceof ObjectModel) {
                        referenceAttributeModel = new ReferenceAttributeModel((ObjectModel) attributeParent, referenceDefinition.getName(), reference);
                    } else if (attributeParent instanceof AttributeModel) {
                        referenceAttributeModel = new ReferenceAttributeModel((AttributeModel) attributeParent, referenceDefinition.getName(), reference);
                    } else {
                        throw new UnsupportedOperationException(attributeParent + " not supported yet");
                    }
                    return referenceAttributeModel;
                }

                @Override
                public AttributeModel visitToOne(ToOneDefinition toOneDefinition) {
                    NestedAttributeModel attributeModel;
                    if (attributeParent instanceof ObjectModel) {
                        attributeModel = new NestedAttributeModel((ObjectModel) attributeParent, toOneDefinition.getName());
                    } else if (attributeParent instanceof AttributeModel) {
                        attributeModel = new NestedAttributeModel((AttributeModel) attributeParent, toOneDefinition.getName());
                    } else {
                        throw new UnsupportedOperationException(attributeParent + " not supported yet");
                    }

                    return createContainer(toOneDefinition, attributeModel);
                }

                @Override
                public AttributeModel visitToMany(ToManyDefinition toManyDefinition) {
                    IndexedAttributeModel attributeModel;
                    if (attributeParent instanceof ObjectModel) {
                        attributeModel = new IndexedAttributeModel((ObjectModel) attributeParent, toManyDefinition.getName());
                    } else if (attributeParent instanceof AttributeModel) {
                        attributeModel = new IndexedAttributeModel((AttributeModel) attributeParent, toManyDefinition.getName());
                    } else {
                        throw new UnsupportedOperationException(attributeParent + " not supported yet");
                    }
                    return createContainer(toManyDefinition, attributeModel);
                }

                private AttributeModel createContainer(NestedAttribute nestedAttribute, AbstractContainerAttributeModel attributeModel) {
                    nestedAttribute.getAttributes().forEach(attribute -> {
                        attributeModel.addAttributeModel(createAttribute(objectCache, attributeModel, attribute));
                    });
                    return attributeModel;
                }

            });
            attributeDefinition.getDefaultValue()
                    .ifPresent(defaultValue -> attributeModel.setDefaultValue(defaultValue));
                    
            objectCache.put(attributeDefinition, attributeModel);
            return attributeModel;
        }

        private Pair<SubjectDefinition, SubjectModel> createSubjectModel(SubjectDefinition subjectDefinition) {
            final SubjectModel subjectModel;
            if (subjectDefinition instanceof UserSubjectDefinition) {
                subjectModel = processModel.addUserSubjectModel(subjectDefinition.getName(), createRoles((UserSubjectDefinition) subjectDefinition));
            } else if (subjectDefinition instanceof ServiceSubjectDefinition) {
                subjectModel = processModel.addServiceSubjectModel(subjectDefinition.getName());
            } else {
                throw new UnsupportedOperationException("subject " + subjectDefinition.getClass().getName() + " not yet implemented");
            }
            if (subjectDefinition.isStarter()) {
                processModel.setStarterSubject(subjectModel);
            }
            return Pair.of(subjectDefinition, subjectModel);
        }

        private List<Role> createRoles(UserSubjectDefinition userSubjectDefinition) {
            return userSubjectDefinition.getRoles().stream()
                    .map(name -> {
                        Role role = roleService.findByName(name)
                                .orElse(new Role(name));
                        //necessary for changeevents
                        roleService.save(role);
                        return role;
                    })
                    .collect(Collectors.toList());
        }

        private Map<StateDefinition, State> createStates() {
            return subjects.entrySet().stream()
                    .flatMap(entry
                            -> entry.getKey().getStates().stream()
                            .map(stateDefinition
                                    -> createState(stateDefinition, entry.getValue()))
                    )
                    .collect(Pair.toMap());
        }

        private Pair<StateDefinition, State> createState(StateDefinition stateDefinition, SubjectModel subjectModel) {
            State state = stateDefinition.accept(new StateDefinitionVisitor<State>() {
                @Override
                public State visitFunctionState(FunctionStateDefinition functionStateDefinition) {
                    return createFunctionState(subjectModel, functionStateDefinition);
                }

                @Override
                public State visitReceiveState(ReceiveStateDefinition receiveStateDefinition) {
                    return createReceiveState(subjectModel, receiveStateDefinition);
                }

                @Override
                public State visitSendState(SendStateDefinition sendStateDefinition) {
                    return createSendState(subjectModel, sendStateDefinition);
                }
            });
            return Pair.<StateDefinition, State>of(stateDefinition, state);
        }

        private State createFunctionState(SubjectModel subjectModel, FunctionStateDefinition stateDefinition) {
            FunctionState functionState = subjectModel.addFunctionState(stateDefinition.getName());
            functionState.setDisplayName(stateDefinition.getDisplayName());
            functionState.setEventType(stateDefinition.getEventType());
            functionState.setProviderName(stateDefinition.getProvider());
            stateDefinition.getParameters().entrySet()
                    .forEach(parameter
                            -> functionState.putParameter(parameter.getKey(), parameter.getValue()));

            stateDefinition.getPermissions().forEach(permissionDefinition -> {
                ObjectCache objectCache = objects.get(permissionDefinition.getObjectDefinition());
                createStatePermissions(objectCache, functionState, permissionDefinition.getAttributePermissions());
            });

            return functionState;
        }

        private void createStatePermissions(ObjectCache objectCache, Object permissionParent,
                List<AttributePermissionDefinition> attributePermissions) {
            attributePermissions.forEach(attributePermissionDefinition -> {
                AttributeModel attributeModel = objectCache.get(attributePermissionDefinition.getAttribute());

                StatePermission statePermission;
                if (permissionParent instanceof FunctionState) {
                    statePermission = ((FunctionState) permissionParent)
                            .addStatePermission(attributeModel, attributePermissionDefinition.getPermission());
                } else if (permissionParent instanceof StatePermission) {
                    statePermission = ((StatePermission) permissionParent)
                            .addChildPermission(attributeModel, attributePermissionDefinition.getPermission());
                } else {
                    throw new UnsupportedOperationException(permissionParent + " not supported yet");
                }
                statePermission.setMandatory(attributePermissionDefinition.isMandatory());

                if (attributePermissionDefinition instanceof NestedPermissionDefinition) {
                    NestedPermissionDefinition nestedPermissionDefinition = (NestedPermissionDefinition) attributePermissionDefinition;
                    createStatePermissions(objectCache, statePermission, nestedPermissionDefinition.getAttributePermissions());
                }
            });
        }

        private State createReceiveState(SubjectModel subjectModel, ReceiveStateDefinition stateDefinition) {
            ReceiveState receiveState = subjectModel.addReceiveState(stateDefinition.getName());
            receiveState.setDisplayName(stateDefinition.getDisplayName());
            receiveState.setEventType(stateDefinition.getEventType());
            return receiveState;
        }

        private State createSendState(SubjectModel subjectModel, SendStateDefinition stateDefinition) {
            SubjectModel receiver = subjects.get(stateDefinition.getReceiver());
            ObjectModel objectModel = objects.get(stateDefinition.getObjectModel()).getObjectModel();

            SendState sendState = subjectModel.addSendState(stateDefinition.getName(), receiver, objectModel);
            sendState.setDisplayName(stateDefinition.getDisplayName());
            sendState.setEventType(stateDefinition.getEventType());
            sendState.setAsync(stateDefinition.isAsync());
            return sendState;
        }

        private void createSubjectStateGraph(SubjectDefinition subjectDefinition) {
            subjectDefinition.getStates().forEach(tail -> {
                tail.accept(new StateDefinitionVisitor<Void>() {
                    @Override
                    public Void visitFunctionState(FunctionStateDefinition functionStateDefinition) {
                        FunctionState functionState = (FunctionState) states.get(functionStateDefinition);
                        functionStateDefinition.getHeads().forEach(head
                                -> functionState.addHead(states.get(head)));
                        return null;
                    }

                    @Override
                    public Void visitReceiveState(ReceiveStateDefinition receiveStateDefinition) {
                        ReceiveState receiveState = (ReceiveState) states.get(receiveStateDefinition);
                        receiveStateDefinition.getTransitions().forEach(transition -> {
                            receiveState.addMessageModel(
                                    objects.get(transition.getObjectDefinition()).getObjectModel(),
                                    states.get(transition.getHead())
                            );

                        });
                        return null;
                    }

                    @Override
                    public Void visitSendState(SendStateDefinition sendStateDefinition) {
                        SendState sendState = (SendState) states.get(sendStateDefinition);
                        if (sendStateDefinition.getHead() != null) {
                            sendState.setHead(states.get(sendStateDefinition.getHead()));
                        }
                        return null;
                    }
                });
            });
        }
    }

    private static class ObjectCache {

        private final ObjectModel objectModel;
        private final Map<AttributeDefinition, AttributeModel> attributes = new HashMap<>();

        public ObjectCache(ObjectModel objectModel) {
            this.objectModel = objectModel;
        }

        public ObjectModel getObjectModel() {
            return objectModel;
        }

        public void put(AttributeDefinition attributeDefinition, AttributeModel attributeModel) {
            attributes.put(attributeDefinition, attributeModel);
        }

        public AttributeModel get(AttributeDefinition attributeDefinition) {
            return attributes.get(attributeDefinition);
        }
    }
}
