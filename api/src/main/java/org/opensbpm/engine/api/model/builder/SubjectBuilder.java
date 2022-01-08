/*******************************************************************************
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
 ******************************************************************************/
package org.opensbpm.engine.api.model.builder;

import org.opensbpm.engine.utils.StreamUtils;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class SubjectBuilder<T extends SubjectBuilder<T, V>, V extends SubjectDefinition> extends AbstractBuilder<V> {

    private final String name;
    private boolean starter;
    private final Map<String,StateBuilder<?, ?>> stateBuilders = new LinkedHashMap<>();

    protected SubjectBuilder(String name) {
        this.name = Objects.requireNonNull(name,"name must be non null");
    }

    public String getName() {
        return name;
    }
    
    public T asStarter() {
        starter = true;
        return castThis();
    }

    public T addState(StateBuilder<?, ?> stateBuilder) {
        stateBuilders.put(stateBuilder.getName(), stateBuilder);
        return castThis();
    }

    public StateBuilder<?, ?> getState(String name) {
        if(!stateBuilders.containsKey(name)) {
            throw new IllegalArgumentException("State '"+ name+"' not found");
        }
        return stateBuilders.get(name);
    }

    @Override
    protected V create() {
        Set<StateBuilder<?, ?>> allStateBuilders = getAllStateBuilders();

        Set<StateDefinition> states = StreamUtils.mapToSet(allStateBuilders, StateBuilder::build);

        allStateBuilders.forEach(StateBuilder::updateHeads);

        return createSubject(new ArrayList<>(states),starter);
    }

    Set<StateBuilder<?, ?>> getAllStateBuilders() {
        Set<StateBuilder<?, ?>> allStateBuilders = new HashSet<>();
        stateBuilders.values().forEach(stateBuilder -> stateBuilder.collectInto(allStateBuilders));
        return allStateBuilders;
    }

    protected abstract V createSubject(List<StateDefinition> states, boolean starter);

}
