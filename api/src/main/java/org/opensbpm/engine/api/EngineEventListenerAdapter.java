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
package org.opensbpm.engine.api;

import org.opensbpm.engine.api.events.ProcessInstanceChangedEvent;
import org.opensbpm.engine.api.events.ProcessModelChangedEvent;
import org.opensbpm.engine.api.events.RoleChangedEvent;
import org.opensbpm.engine.api.events.RoleUserChangedEvent;
import org.opensbpm.engine.api.events.UserChangedEvent;
import org.opensbpm.engine.api.events.UserProcessInstanceChangedEvent;
import org.opensbpm.engine.api.events.UserProcessModelChangedEvent;
import org.opensbpm.engine.api.events.UserTaskChangedEvent;

public abstract class EngineEventListenerAdapter implements EngineEventListener {

    @Override
    public void handleProcessInstanceChangedEvent(ProcessInstanceChangedEvent changedEvent) {
    }

    @Override
    public void handleProcessModelChangedEvent(ProcessModelChangedEvent changedEvent) {
    }

    @Override
    public void handleRoleChangedEvent(RoleChangedEvent changedEvent) {
    }

    @Override
    public void handleRoleUserChangedEvent(RoleUserChangedEvent changedEvent) {
    }

    @Override
    public void handleUserChangedEvent(UserChangedEvent changedEvent) {
    }

    @Override
    public void handleUserProcessInstanceChangedEvent(UserProcessInstanceChangedEvent changedEvent) {
    }

    @Override
    public void handleUserProcessModelChangedEvent(UserProcessModelChangedEvent changedEvent) {
    }

    @Override
    public void handleUserTaskChangedEvent(UserTaskChangedEvent changedEvent) {
    }

}
