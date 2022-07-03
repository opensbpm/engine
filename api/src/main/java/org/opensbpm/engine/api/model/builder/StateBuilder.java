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

import java.util.Set;
import java.util.logging.Logger;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;

public abstract class StateBuilder<T extends StateBuilder<T, V>, V extends StateDefinition> extends AbstractBuilder<V,T> {

    protected static final Logger LOGGER = Logger.getLogger(StateBuilder.class.getName());

    protected final String name;
    private String displayName;
    private StateEventType eventType;

    protected StateBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * add {@link StateDefinition#displayName} to this {@link StateDefinition}
     *
     * @param displayName {@link StateDefinition#displayName}
     * @return
     */
    public T withDisplayName(String displayName) {
        checkBuilt();
        this.displayName = displayName;
        return self();
    }

    public T asStart() {
        return eventType(StateEventType.START);
    }

    public T asEnd() {
        return eventType(StateEventType.END);
    }

    public T eventType(StateEventType eventType) {
        checkBuilt();
        this.eventType = eventType;
        return self();
    }

    @Override
    protected final V create() {
        return createState(displayName, eventType);
    }

    protected abstract V createState(String displayName, StateEventType eventType);

    protected abstract void collectInto(Set<StateBuilder<?, ?>> stateBuilders);

    protected abstract void updateHeads();

    public static class AbstractStateDefinition {

        private final String name;
        private final String displayName;
        private final StateEventType eventType;

        protected AbstractStateDefinition(String name, String displayName, StateEventType eventType) {
            this.name = name;
            this.displayName = displayName;
            this.eventType = eventType;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public StateEventType getEventType() {
            return eventType;
        }

    }

}
