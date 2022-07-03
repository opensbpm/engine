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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition.ReceiveTransitionDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.mapToList;

public class ReceiveStateBuilder extends StateBuilder<ReceiveStateBuilder, ReceiveStateDefinition> {

    private final List<TransitionBuilder> transitionBuilders = new ArrayList<>();

    public ReceiveStateBuilder(String name) {
        super(name);
    }

    @Override
    protected ReceiveStateBuilder self() {
        return this;
    }

    public ReceiveStateBuilder toHead(ObjectBuilder objectBuilder, StateBuilder<?, ?> headBuilder) {
        LOGGER.log(Level.FINER, "''{0}'' to head ''{1}''", new Object[]{name, headBuilder.name});
        checkBuilt();
        transitionBuilders.add(new TransitionBuilder(objectBuilder, headBuilder));
        return this;
    }

    @Override
    protected ReceiveStateDefinition createState(String displayName, StateEventType eventType) {
        List<ReceiveTransitionDefinition> transitions = mapToList(transitionBuilders, TransitionBuilder::build);
        return new ReceiveStateDefinitionImpl(name, displayName, eventType, transitions);
    }

    @Override
    protected void collectInto(Set<StateBuilder<?, ?>> stateBuilders) {
        if (!stateBuilders.contains(this)) {
            stateBuilders.add(this);
            transitionBuilders.forEach(builder -> builder.headBuilder.collectInto(stateBuilders));
        }
    }

    @Override
    protected void updateHeads() {
        //noop
    }

    private class TransitionBuilder extends AbstractBuilder<ReceiveTransitionDefinition, TransitionBuilder> {

        private final StateBuilder<?, ?> headBuilder;
        private final ObjectBuilder objectBuilder;

        private TransitionBuilder(ObjectBuilder objectBuilder, StateBuilder<?, ?> headBuilder) {
            this.headBuilder = headBuilder;
            this.objectBuilder = objectBuilder;
        }

        @Override
        protected TransitionBuilder self() {
            return this;
        }

        @Override
        protected ReceiveTransitionDefinition create() {
            ObjectDefinition objectDefinition = objectBuilder.build();
            StateDefinition headState = headBuilder.build();
            return new ReceiveTransitionDefinition() {
                @Override
                public ObjectDefinition getObjectDefinition() {
                    return objectDefinition;
                }

                @Override
                public StateDefinition getHead() {
                    return headState;
                }
            };
        }
    }

    private class ReceiveStateDefinitionImpl extends AbstractStateDefinition
            implements ReceiveStateDefinition {

        private final List<ReceiveTransitionDefinition> transitions;

        public ReceiveStateDefinitionImpl(String name, String displayName, StateEventType eventType, List<ReceiveTransitionDefinition> transitions) {
            super(name, displayName, eventType);
            this.transitions = new ArrayList<>(transitions);
        }

        @Override
        public List<ReceiveTransitionDefinition> getTransitions() {
            return emptyOrUnmodifiableList(transitions);
        }
    }

}
