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
package org.opensbpm.engine.api.model.definition;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlTransient;
import static org.opensbpm.engine.utils.StreamUtils.flatMapToList;

@XmlTransient
public class SubjectStateGraph {

    private final Map<? extends StateDefinition, List<StateDefinition>> verteces;

    public SubjectStateGraph(SubjectDefinition subjectDefinition) {
        verteces = subjectDefinition.getStates().stream()
                .filter(tail -> !tail.getHeads().isEmpty())
                .collect(Collectors.toMap(tail -> tail, StateDefinition::getHeads));
    }

    public List<StateDefinition> getRoots() {
        List<StateDefinition> tails = getTails();
        tails.removeAll(getHeads());
        return tails;
    }

    public List<StateDefinition> getLeafs() {
        List<StateDefinition> heads = getHeads();
        heads.removeAll(getTails());
        return heads;
    }

    public List<StateDefinition> getHeads() {
        return flatMapToList(verteces.values(), Collection::stream);
    }

    public List<StateDefinition> getTails() {
        return verteces.keySet().stream()
                .collect(Collectors.toList());
    }

    public List<StateDefinition> getHeadsOf(StateDefinition tail) {
        return verteces.containsKey(tail) ? verteces.get(tail) : Collections.emptyList();
    }

}
