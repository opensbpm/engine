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
package org.opensbpm.engine.api.model.builder;

import java.util.*;
import java.util.logging.Level;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.SendStateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;

public class SendStateBuilder extends StateBuilder<SendStateBuilder, SendStateDefinition> {

    private final SubjectBuilder<?, ?> receiverBuilder;
    private final ObjectBuilder objectBuilder;
    private boolean async;
    private Optional<StateBuilder<?, ?>> headBuilder = Optional.empty();

    public SendStateBuilder(String name, SubjectBuilder<?, ?> receiverBuilder, ObjectBuilder objectBuilder) {
        super(name);
        this.receiverBuilder = Objects.requireNonNull(receiverBuilder, "receiverBuilder must be non null");
        this.objectBuilder = Objects.requireNonNull(objectBuilder, "objectBuilder must be non null");
    }

    @Override
    protected SendStateBuilder self() {
        return this;
    }

    public SendStateBuilder asAsync() {
        checkBuilt();
        this.async = true;
        return self();
    }

    public SendStateBuilder toHead(StateBuilder<?, ?> headBuilder) {
        LOGGER.log(Level.FINER, "''{0}'' to head ''{1}''", new Object[]{name, headBuilder.name});
        checkBuilt();
        this.headBuilder = Optional.of(headBuilder);
        return self();
    }

    @Override
    protected SendStateDefinition createState(String displayName, StateDefinition.StateEventType eventType) {
        ObjectDefinition objectDefinition = objectBuilder.build();
        return new SendStateDefinitionImpl(name, displayName, eventType, objectDefinition);
    }

    @Override
    protected void collectInto(Set<StateBuilder<?, ?>> stateBuilders) {
        if (!stateBuilders.contains(this)) {
            stateBuilders.add(this);
            headBuilder.ifPresent(builder -> builder.collectInto(stateBuilders));
        }
    }

    @Override
    protected void updateHeads() {
        headBuilder.ifPresent(builder -> ((SendStateDefinitionImpl) build()).setHead(builder.build()));
    }

    void updateReceiver() {
        ((SendStateDefinitionImpl) build()).setReceiver(receiverBuilder.build());
    }

    private class SendStateDefinitionImpl extends AbstractStateDefinition implements SendStateDefinition {

        private final ObjectDefinition objectDefinition;
        private SubjectDefinition receiver;
        private StateDefinition head;

        public SendStateDefinitionImpl(String name, String displayName, StateEventType eventType, ObjectDefinition objectDefinition) {
            super(name, displayName, eventType);
            this.objectDefinition = objectDefinition;
        }

        @Override
        public SubjectDefinition getReceiver() {
            return receiver;
        }

        public void setReceiver(SubjectDefinition receiver) {
            this.receiver = receiver;
        }

        @Override
        public ObjectDefinition getObjectModel() {
            return objectDefinition;
        }

        public void setHead(StateDefinition head) {
            this.head = head;
        }

        @Override
        public StateDefinition getHead() {
            return head;
        }

        public List<StateDefinition> getHeads() {
            return head == null ? Collections.emptyList() : Arrays.asList(head);
        }

        @Override
        public boolean isAsync() {
            return async;
        }
    }
}
