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
package org.opensbpm.engine.api.junit;

import org.opensbpm.engine.api.model.definition.PermissionDefinition;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;

import static org.opensbpm.engine.utils.StreamUtils.filterToOne;

public final class ModelUtils {

    public static SubjectDefinition getSubject(ProcessDefinition processDefinition, String name) {
        return filterToOne(processDefinition.getSubjects(), subject -> subject.getName().equals(name))
                .orElseThrow(() -> new IllegalArgumentException("Subject with name " + name + " not found."));
    }

    public static StateDefinition getState(ProcessDefinition processDefinition, String subjectName,String statename) {
        return filterToOne(getSubject(processDefinition, subjectName).getStates(), state -> state.getName().equals(statename))
                .orElseThrow(() -> new IllegalArgumentException("State with name " + statename + " not found."));
    }

    public static PermissionDefinition getPermission(FunctionStateDefinition stateDefinition,String name) {
        return filterToOne(stateDefinition.getPermissions(), permission -> permission.getObjectDefinition().getName().equals(name))
                .orElseThrow(() -> new IllegalArgumentException("Object with name " + name + " not found."));
    }

    private ModelUtils() {
    }

}
