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
package org.opensbpm.engine.api.model.definition;

import static org.opensbpm.engine.utils.StreamUtils.mapToList;

import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition.ReceiveTransitionDefinition;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlType;

public interface StateDefinition {

    String getName();

    String getDisplayName();

    StateEventType getEventType();

    List<StateDefinition> getHeads();

    <T> T accept(StateDefinitionVisitor<T> visitor);

    public interface FunctionStateDefinition extends StateDefinition {

        String getProvider();

        Map<String, String> getParameters();

        List<PermissionDefinition> getPermissions();

        @Override
        List<StateDefinition> getHeads();

        @Override
        default <T> T accept(StateDefinitionVisitor<T> visitor) {
            return visitor.visitFunctionState(this);
        }

    }

    public interface ReceiveStateDefinition extends StateDefinition {

        List<ReceiveTransitionDefinition> getTransitions();

        @Override
        default List<StateDefinition> getHeads() {
            return mapToList(getTransitions(), ReceiveTransitionDefinition::getHead);
        }

        @Override
        default <T> T accept(StateDefinitionVisitor<T> visitor) {
            return visitor.visitReceiveState(this);
        }

        public interface ReceiveTransitionDefinition {

            ObjectDefinition getObjectDefinition();

            StateDefinition getHead();

        }
    }

    public interface SendStateDefinition extends StateDefinition {

        SubjectDefinition getReceiver();

        ObjectDefinition getObjectModel();

        StateDefinition getHead();

        @Override
        List<StateDefinition> getHeads();

        boolean isAsync();

        @Override
        default <T> T accept(StateDefinitionVisitor<T> visitor) {
            return visitor.visitSendState(this);
        }

    }

    @XmlType
    public enum StateEventType {
        START, END
    }
}
