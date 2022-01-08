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
package org.opensbpm.engine.core.model;

import org.opensbpm.engine.api.model.builder.FunctionStateBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.AbstractAttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.AttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.NestedAttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.PermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.ToManyPermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.ToOnePermissionBuilder;
import org.opensbpm.engine.api.model.builder.ReceiveStateBuilder;
import org.opensbpm.engine.api.model.builder.SendStateBuilder;
import org.opensbpm.engine.api.model.builder.StateBuilder;
import org.opensbpm.engine.api.model.builder.SubjectBuilder;
import org.opensbpm.engine.core.model.ObjectModelConverter.ObjectModelCache;
import org.opensbpm.engine.core.model.entities.AttributeModelVisitor;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.IndexedAttributeModel;
import org.opensbpm.engine.core.model.entities.MessageModel;
import org.opensbpm.engine.core.model.entities.NestedAttributeModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.ReferenceAttributeModel;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.SimpleAttributeModel;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.StateGraph;
import org.opensbpm.engine.core.model.entities.StatePermission;
import org.opensbpm.engine.core.model.entities.StateVisitor;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class StateModelConverter {

    private final ObjectModelCache objectModelCache;
    private final Map<SubjectModel, SubjectBuilder<?, ?>> subjects;

    public StateModelConverter(ObjectModelCache objectModelCache, Map<SubjectModel, SubjectBuilder<?, ?>> subjects) {
        this.objectModelCache = objectModelCache;
        this.subjects = subjects;
    }

    public StateBuilder<?, ?> createStateBuilder(State state) {
        StateBuilder<?, ?> stateBuilder = state.accept(new StateVisitor<StateBuilder<?, ?>>() {
            @Override
            public StateBuilder<?, ?> visitFunctionState(FunctionState functionState) {
                FunctionStateBuilder functionStateDefinition = new FunctionStateBuilder(state.getName());

                MultiValueMap<ObjectModel, StatePermission> permissions = new LinkedMultiValueMap<>();
                functionState.getStatePermissions().forEach(statePermission -> {
                    permissions.add(objectModelCache.findObjectModel(statePermission.getAttributeModel()), statePermission);
                });
                permissions.forEach((objectModel, statePermissions) -> {
                    PermissionBuilder permission = new PermissionBuilder(objectModelCache.getObjectBuilder(objectModel));
                    statePermissions.forEach(statePermission -> {
                        permission.addPermission(createAttributePermissionBuilder(objectModel, statePermission, permission));
                    });

                    functionStateDefinition.addPermission(permission);
                });
                return functionStateDefinition;
            }

            @Override
            public StateBuilder<?, ?> visitReceiveState(ReceiveState receiveState) {
                return new ReceiveStateBuilder(state.getName());
            }

            @Override
            public StateBuilder<?, ?> visitSendState(SendState sendState) {
                SendStateBuilder sendStateDefinition = new SendStateBuilder(
                        state.getName(),
                        subjects.get(sendState.getReceiver()),
                        objectModelCache.getObjectBuilder(sendState.getObjectModel())
                );
                if (sendState.isAsync()) {
                    sendStateDefinition.asAsync();
                }
                return sendStateDefinition;
            }

        });
        stateBuilder.withDisplayName(state.getDisplayName());
        stateBuilder.eventType(state.getEventType());
        return stateBuilder;
    }

    private AbstractAttributePermissionBuilder<?, ?> createAttributePermissionBuilder(ObjectModel objectModel, StatePermission statePermission, PermissionBuilder permission) {
        return statePermission.getAttributeModel().accept(new AttributeModelVisitor<AbstractAttributePermissionBuilder<?, ?>>() {
            @Override
            public AttributePermissionBuilder visitSimple(SimpleAttributeModel attributeModel) {
                return new AttributePermissionBuilder(
                        objectModelCache.getAttributeBuilder(objectModel, statePermission.getAttributeModel()),
                        statePermission.getPermission(),
                        statePermission.isMandatory()
                );
            }

            @Override
            public AttributePermissionBuilder visitReference(ReferenceAttributeModel attributeModel) {
                return new AttributePermissionBuilder(
                        objectModelCache.getAttributeBuilder(objectModel, statePermission.getAttributeModel()),
                        statePermission.getPermission(),
                        statePermission.isMandatory()
                );
            }

            @Override
            public ToOnePermissionBuilder visitNested(NestedAttributeModel attributeModel) {
                ToOnePermissionBuilder toOnePermissionBuilder = new ToOnePermissionBuilder(
                        objectModelCache.getAttributeBuilder(objectModel, statePermission.getAttributeModel()),
                        statePermission.getPermission(),
                        statePermission.isMandatory()
                );
                statePermission.getChildPermissions()
                        .forEach(childPermission -> {
                            toOnePermissionBuilder.addPermission(createAttributePermissionBuilder(objectModel, childPermission, permission));
                        });
                return toOnePermissionBuilder;
            }

            @Override
            public ToManyPermissionBuilder visitIndexed(IndexedAttributeModel attributeModel) {
                ToManyPermissionBuilder toManyPermissionBuilder = new ToManyPermissionBuilder(
                        objectModelCache.getAttributeBuilder(objectModel, statePermission.getAttributeModel()),
                        statePermission.getPermission(),
                        statePermission.isMandatory()
                );
                statePermission.getChildPermissions()
                        .forEach(childPermission -> {
                            toManyPermissionBuilder.addPermission(createAttributePermissionBuilder(objectModel, childPermission, permission));
                        });
                return toManyPermissionBuilder;
            }

        });
    }

    public void createStateGraph(SubjectModel subjectModel, Map<State, StateBuilder<?, ?>> states) {
        StateGraph stateGraph = new StateGraph(subjectModel);
        stateGraph.getTails().forEach(state
                -> state.accept(new StateVisitor<Void>() {

                    @Override
                    public Void visitFunctionState(FunctionState functionState) {
                        FunctionStateBuilder functionBuilder = (FunctionStateBuilder) states.get(functionState);
                        for (State head : functionState.getHeads()) {
                            functionBuilder.toHead(states.get(head));
                        }
                        return null;
                    }

                    @Override
                    public Void visitReceiveState(ReceiveState receiveState) {
                        ReceiveStateBuilder receiveBuilder = (ReceiveStateBuilder) states.get(receiveState);
                        for (MessageModel messageModel : receiveState.getMessageModels()) {
                            receiveBuilder.toHead(
                                    objectModelCache.getObjectBuilder(messageModel.getObjectModel()),
                                    states.get(messageModel.getHead())
                            );
                        }
                        return null;
                    }

                    @Override
                    public Void visitSendState(SendState sendState) {
                        SendStateBuilder sendBuilder = (SendStateBuilder) states.get(sendState);
                        if (sendState.getHead() != null) {
                            sendBuilder.toHead(states.get(sendState.getHead()));
                        }
                        return null;
                    }
                })
        );
    }
}
