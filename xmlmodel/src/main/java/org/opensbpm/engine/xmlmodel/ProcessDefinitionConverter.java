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

import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinitionVisitor;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.FieldDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ReferenceDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToManyDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToOneDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.AttributePermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.ToManyPermission;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.ToOnePermission;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.SendStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinitionVisitor;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.ServiceSubjectDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.UserSubjectDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.opensbpm.engine.utils.StreamUtils;
import org.opensbpm.engine.xmlmodel.processmodel.AttributePermissionType;
import org.opensbpm.engine.xmlmodel.processmodel.Field;
import org.opensbpm.engine.xmlmodel.processmodel.FieldType;
import org.opensbpm.engine.xmlmodel.processmodel.FunctionStateType;
import org.opensbpm.engine.xmlmodel.processmodel.ObjectFactory;
import org.opensbpm.engine.xmlmodel.processmodel.ObjectType;
import org.opensbpm.engine.xmlmodel.processmodel.ParametersWrapper;
import org.opensbpm.engine.xmlmodel.processmodel.Permission;
import org.opensbpm.engine.xmlmodel.processmodel.PermissionType;
import org.opensbpm.engine.xmlmodel.processmodel.ProcessType;
import org.opensbpm.engine.xmlmodel.processmodel.ReceiveStateType;
import org.opensbpm.engine.xmlmodel.processmodel.ReceiveTransitionType;
import org.opensbpm.engine.xmlmodel.processmodel.ReferenceType;
import org.opensbpm.engine.xmlmodel.processmodel.SendStateType;
import org.opensbpm.engine.xmlmodel.processmodel.ServiceSubject;
import org.opensbpm.engine.xmlmodel.processmodel.StateEventType;
import org.opensbpm.engine.xmlmodel.processmodel.StateType;
import org.opensbpm.engine.xmlmodel.processmodel.SubjectType;
import org.opensbpm.engine.xmlmodel.processmodel.ToManyPermissionType;
import org.opensbpm.engine.xmlmodel.processmodel.ToManyType;
import org.opensbpm.engine.xmlmodel.processmodel.ToOnePermissionType;
import org.opensbpm.engine.xmlmodel.processmodel.ToOneType;
import org.opensbpm.engine.xmlmodel.processmodel.UserSubject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class ProcessDefinitionConverter {

    private Map<ObjectDefinition, ObjectType> objects;
    private Map<SubjectDefinition, SubjectType> subjects;
    private Map<StateDefinition, StateType> states;

    public ProcessType convert(ProcessDefinition definition) {
        ProcessType processType = new ObjectFactory().createProcessType();
        processType.setName(definition.getName());
        processType.setVersion(definition.getVersion());
        //TODO processType.setState(definition.getState());
        processType.setDescription(definition.getDescription());

        objects = definition.getObjects().stream()
                .map(this::createObjectModel)
                .collect(StreamUtils.toMap());
        objects.values().forEach(objectType -> processType.getObject().add(objectType));

        subjects = definition.getSubjects().stream()
                .map(this::createSubjectModel)
                .collect(StreamUtils.toMap());

        states = subjects.entrySet().stream()
                .flatMap(entry -> createState(entry))
                .collect(StreamUtils.toMap());

        subjects.keySet().forEach(this::createSubjectStateGraph);
        subjects.values().forEach(subjectType -> processType.getUserSubjectOrServiceSubject().add(subjectType));

        return processType;
    }

    private Pair<ObjectDefinition, ObjectType> createObjectModel(ObjectDefinition objectDefinition) {
        ObjectType objectType = new ObjectFactory().createObjectType();
        objectType.setName(objectDefinition.getName());
        objectType.setDisplayName(objectDefinition.getDisplayName());

        objectDefinition.getAttributes().forEach(attributeDefinition
                -> objectType.getFieldOrReferenceOrToOne().add(createAttribute(objectType, attributeDefinition)));
        return Pair.of(objectDefinition, objectType);
    }

    private Object createAttribute(ObjectType objectType, AttributeDefinition attributeDefinition) {
        return attributeDefinition.accept(new AttributeDefinitionVisitor<Object>() {
            @Override
            public Object visitField(FieldDefinition fieldDefinition) {
                return createFieldAttribute(fieldDefinition);
            }

            @Override
            public Object visitReference(ReferenceDefinition referenceDefinition) {
                return createReferenceAttribute(referenceDefinition);
            }

            @Override
            public Object visitToOne(ToOneDefinition toOneDefinition) {
                return createToOneAttribute(objectType, toOneDefinition);
            }

            @Override
            public Object visitToMany(ToManyDefinition toManyDefinition) {
                return createToManyAttribute(objectType, toManyDefinition);
            }

        });
    }

    private Field createFieldAttribute(FieldDefinition fieldDefinition) {
        Field field = new ObjectFactory().createField();
        field.setValue(fieldDefinition.getName());
        field.setType(FieldType.fromValue(fieldDefinition.getFieldType().name()));
        return field;
    }

    private ReferenceType createReferenceAttribute(ReferenceDefinition referenceDefinition) {
        ReferenceType referenceType = new ObjectFactory().createReferenceType();
        referenceType.setValue(referenceDefinition.getName());
        referenceType.setObject(referenceDefinition.getObjectDefinition().getName());
        return referenceType;
    }

    private ToOneType createToOneAttribute(ObjectType objectCache, ToOneDefinition toOneDefinition) {
        ToOneType toOneType = new ObjectFactory().createToOneType();
        toOneType.setName(toOneDefinition.getName());
        for (AttributeDefinition attributeDefinition : toOneDefinition.getAttributes()) {
            toOneType.getFieldOrReferenceOrToOne().add(createAttribute(objectCache, attributeDefinition));
        }
        return toOneType;
    }

    private ToManyType createToManyAttribute(ObjectType objectCache, ToManyDefinition toManyDefinition) {
        ToManyType toManyType = new ObjectFactory().createToManyType();
        toManyType.setName(toManyDefinition.getName());
        for (AttributeDefinition attributeDefinition : toManyDefinition.getAttributes()) {
            toManyType.getFieldOrReferenceOrToOne().add(createAttribute(objectCache, attributeDefinition));
        }
        return toManyType;
    }

    private Pair<SubjectDefinition, SubjectType> createSubjectModel(SubjectDefinition subjectDefinition) {
        SubjectType subjectType;
        if (subjectDefinition instanceof UserSubjectDefinition) {
            UserSubject userSubject = new ObjectFactory().createUserSubject();
            userSubject.setName(subjectDefinition.getName());
            userSubject.getRoles().addAll(((UserSubjectDefinition) subjectDefinition).getRoles());
            subjectType = userSubject;
        } else if (subjectDefinition instanceof ServiceSubjectDefinition) {
            ServiceSubject serviceSubject = new ObjectFactory().createServiceSubject();
            serviceSubject.setName(subjectDefinition.getName());
            subjectType = serviceSubject;
        } else {
            throw new UnsupportedOperationException("subject " + subjectDefinition.getClass().getName() + " not yet implemented");
        }
        if (subjectDefinition.isStarter()) {
            subjectType.setStarter(Boolean.TRUE);
        }
        return Pair.of(subjectDefinition, subjectType);
    }

    private Stream<Pair<StateDefinition, StateType>> createState(Map.Entry<SubjectDefinition, SubjectType> entry) {
        return entry.getKey().getStates().stream()
                .map(stateDefinition -> createState(stateDefinition, entry.getValue()));
    }

    private Pair<StateDefinition, StateType> createState(StateDefinition stateDefinition, SubjectType subjectType) {
        StateType stateType = stateDefinition.accept(new StateDefinitionVisitor<StateType>() {
            @Override
            public StateType visitFunctionState(FunctionStateDefinition functionStateDefinition) {
                return createFunctionState(functionStateDefinition);
            }

            @Override
            public StateType visitReceiveState(ReceiveStateDefinition receiveStateDefinition) {
                return createReceiveState(receiveStateDefinition);
            }

            @Override
            public StateType visitSendState(SendStateDefinition sendStateDefinition) {
                return createSendState(sendStateDefinition);
            }
        });
        subjectType.getFunctionStateOrSendStateOrReceiveState().add(stateType);
        return Pair.<StateDefinition, StateType>of(stateDefinition, stateType);
    }

    private FunctionStateType createFunctionState(FunctionStateDefinition stateDefinition) {
        FunctionStateType functionStateType = new ObjectFactory().createFunctionStateType();
        functionStateType.setName(stateDefinition.getName());
        functionStateType.setDisplayName(stateDefinition.getDisplayName());
        if (stateDefinition.getEventType() != null) {
            functionStateType.setEventType(StateEventType.fromValue(stateDefinition.getEventType().name()));
        }
        functionStateType.setProvider(stateDefinition.getProvider());
        ParametersWrapper parametersWrapper = new ObjectFactory().createParametersWrapper();

        Document document = createDocument();
        stateDefinition.getParameters().entrySet()
                .forEach(entry
                        -> {
                    Element element = document.createElement(entry.getKey());
                    Text textNode = document.createTextNode(entry.getValue());
                    element.appendChild(textNode);
                    parametersWrapper.getAny().add(element);
                });
        functionStateType.setParameters(parametersWrapper);

        for (PermissionDefinition permission : stateDefinition.getPermissions()) {
            PermissionType permissionType = new ObjectFactory().createPermissionType();
            permissionType.setObject(permission.getObjectDefinition().getName());
            List<Object> attributePermissionTypes = createAtributePermission(permission.getAttributePermissions());
            permissionType.getFieldOrToOneOrToMany().addAll(attributePermissionTypes);
            functionStateType.getPermission().add(permissionType);
        }

        return functionStateType;
    }

    private List<Object> createAtributePermission(List<AttributePermissionDefinition> attributePermissions) {
        List<Object> attributePermissionTypes = new ArrayList<>();
        for (AttributePermissionDefinition attributePermission : attributePermissions) {
            Object permissionType;
            if (attributePermission instanceof ToOnePermission) {
                ToOnePermissionType toOnePermissionType = new ObjectFactory().createToOnePermissionType();
                toOnePermissionType.setName(attributePermission.getAttribute().getName());
                toOnePermissionType.getFieldOrToOneOrToMany().addAll(createAtributePermission(((ToOnePermission) attributePermission).getAttributePermissions()));
                permissionType = toOnePermissionType;
            } else if (attributePermission instanceof ToManyPermission) {
                ToManyPermissionType toManyPermissionType = new ObjectFactory().createToManyPermissionType();
                toManyPermissionType.setName(attributePermission.getAttribute().getName());
                toManyPermissionType.getFieldOrToOneOrToMany().addAll(createAtributePermission(((ToManyPermission) attributePermission).getAttributePermissions()));
                permissionType = toManyPermissionType;
            } else {
                AttributePermissionType attributePermissionType = new ObjectFactory().createAttributePermissionType();
                attributePermissionType.setValue(attributePermission.getAttribute().getName());
                attributePermissionType.setPermission(Permission.fromValue(attributePermission.getPermission().name()));
                attributePermissionType.setMandatory(attributePermission.isMandatory());

                permissionType = attributePermissionType;
            }
            attributePermissionTypes.add(permissionType);
        }
        return attributePermissionTypes;
    }

    private Document createDocument() {
        try {
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder builder = df.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ReceiveStateType createReceiveState(ReceiveStateDefinition stateDefinition) {
        ReceiveStateType receiveStateType = new ObjectFactory().createReceiveStateType();
        receiveStateType.setName(stateDefinition.getName());
        receiveStateType.setDisplayName(stateDefinition.getDisplayName());
        if (stateDefinition.getEventType() != null) {
            receiveStateType.setEventType(StateEventType.fromValue(stateDefinition.getEventType().name()));
        }
        return receiveStateType;
    }

    private SendStateType createSendState(SendStateDefinition stateDefinition) {
        SubjectType receiver = subjects.get(stateDefinition.getReceiver());
        ObjectType objectType = objects.get(stateDefinition.getObjectModel());

        SendStateType sendStateType = new ObjectFactory().createSendStateType();
        sendStateType.setName(stateDefinition.getName());
        sendStateType.setDisplayName(stateDefinition.getDisplayName());
        if (stateDefinition.getEventType() != null) {
            sendStateType.setEventType(StateEventType.fromValue(stateDefinition.getEventType().name()));
        }
        sendStateType.setAsync(stateDefinition.isAsync());
        sendStateType.setReceiver(receiver.getName());
        sendStateType.setMessage(objectType.getName());
        //sendStateType.setToState();
        return sendStateType;
    }

    private void createSubjectStateGraph(SubjectDefinition subjectDefinition) {
        subjectDefinition.getStates().forEach(tail -> {
            tail.accept(new StateDefinitionVisitor<Void>() {
                @Override
                public Void visitFunctionState(FunctionStateDefinition functionStateDefinition) {
                    FunctionStateType functionState = (FunctionStateType) states.get(functionStateDefinition);
                    functionStateDefinition.getHeads().forEach(head
                            -> functionState.getToState().add(states.get(head).getName()));
                    return null;
                }

                @Override
                public Void visitReceiveState(ReceiveStateDefinition receiveStateDefinition) {
                    ReceiveStateType receiveState = (ReceiveStateType) states.get(receiveStateDefinition);
                    receiveStateDefinition.getTransitions().forEach(transition -> {
                        ReceiveTransitionType receiveTransitionType = new ObjectFactory().createReceiveTransitionType();
                        receiveTransitionType.setObject(objects.get(transition.getObjectDefinition()).getName());
                        receiveTransitionType.setToState(states.get(transition.getHead()).getName());
                        receiveState.getMessage().add(receiveTransitionType);

                    });
                    return null;
                }

                @Override
                public Void visitSendState(SendStateDefinition sendStateDefinition) {
                    SendStateType sendState = (SendStateType) states.get(sendStateDefinition);
                    if (sendStateDefinition.getHead() != null) {
                        sendState.setToState(states.get(sendStateDefinition.getHead()).getName());
                    }
                    return null;
                }
            });
        });
    }

}
