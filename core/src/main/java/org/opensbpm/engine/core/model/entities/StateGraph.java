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
package org.opensbpm.engine.core.model.entities;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class StateGraph {

    private final Map<State, Collection<State>> vertices;

    public StateGraph(SubjectModel subjectModel) {
        vertices = subjectModel.getStates().stream()
                .filter(state -> !state.getHeads().isEmpty())
                .collect(Collectors.toMap(tail -> tail, State::getHeads));
    }

    public Collection<State> getRoots() {
        Collection<State> tails = getTails();
        tails.removeAll(getHeads());
        return tails;
    }

    public Collection<State> getLeafs() {
        Collection<State> heads = getHeads();
        heads.removeAll(getTails());
        return heads;
    }

    public Collection<State> getHeads() {
        return vertices.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Collection<State> getTails() {
        return vertices.keySet().stream()
                .collect(Collectors.toList());
    }
}
