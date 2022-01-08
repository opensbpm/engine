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

import org.opensbpm.engine.api.events.EngineEvent;
import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.events.ProcessInstanceChangedEvent;
import org.opensbpm.engine.api.events.ProcessModelChangedEvent;
import org.opensbpm.engine.api.events.ProviderTaskChangedEvent;
import org.opensbpm.engine.api.events.RoleChangedEvent;
import org.opensbpm.engine.api.events.RoleUserChangedEvent;
import org.opensbpm.engine.api.events.UserChangedEvent;
import org.opensbpm.engine.api.events.UserProcessInstanceChangedEvent;
import org.opensbpm.engine.api.events.UserProcessModelChangedEvent;
import org.opensbpm.engine.api.events.UserTaskChangedEvent;
import org.opensbpm.engine.api.instance.TaskInfo;

import static org.opensbpm.engine.api.junit.CommonMatchers.value;

import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.instance.RoleToken;
import org.opensbpm.engine.api.instance.UserToken;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.is;

public final class EngineEventMatcher {

    public static <T extends EngineEvent<?>> Matcher<? super T> isRoleChangedEvent(String roleName, Type type) {
        return allOf(instanceOf(RoleChangedEvent.class),
                isType(RoleChangedEvent.class, type),
                value(RoleChangedEvent.class, RoleChangedEvent::getSource, value("name", RoleToken::getName, is(roleName)))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isRoleUserChangedEvent(Long userId, Type type) {
        return allOf(instanceOf(RoleUserChangedEvent.class),
                isType(RoleUserChangedEvent.class, type),
                value(RoleUserChangedEvent.class, RoleUserChangedEvent::getSource, value(UserToken::getId, is(userId)))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isUserChangedEvent(String userName, Type type) {
        return allOf(instanceOf(UserChangedEvent.class),
                isType(UserChangedEvent.class, type),
                value(UserChangedEvent.class, UserChangedEvent::getSource, value("name", UserToken::getName, is(userName)))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isUserChangedEvent(Long userId, Type type) {
        return allOf(instanceOf(UserChangedEvent.class),
                isType(UserChangedEvent.class, type),
                value(UserChangedEvent.class, UserChangedEvent::getSource, value(UserToken::getId, is(userId)))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isProcessModelChangedEvent(String modelName, Type type) {
        return allOf(
                instanceOf(ProcessModelChangedEvent.class),
                isType(ProcessModelChangedEvent.class, type),
                value(ProcessModelChangedEvent.class, ProcessModelChangedEvent::getSource, value(ProcessModelInfo::getName, is(modelName)))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<T> isUserProcessModelChangedEvent(Long userId) {
        return allOf(
                instanceOf(UserProcessModelChangedEvent.class),
                value(UserProcessModelChangedEvent.class, UserProcessModelChangedEvent::getUserId, is(userId))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<T> isUserProcessModelChangedEvent(Type type) {
        return allOf(
                instanceOf(UserProcessModelChangedEvent.class),
                isType(UserProcessModelChangedEvent.class, type)
        );
    }

    public static <T extends EngineEvent<?>> Matcher<T> isUserProcessModelChangedEvent(Long userId, Type type) {
        return allOf(
                instanceOf(UserProcessModelChangedEvent.class),
                value(UserProcessModelChangedEvent.class, UserProcessModelChangedEvent::getUserId, is(userId)),
                isType(UserProcessModelChangedEvent.class, type)
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isProcessInstanceChangedEvent(Type type) {
        return allOf(
                instanceOf(ProcessInstanceChangedEvent.class),
                isType(ProcessInstanceChangedEvent.class, type)
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isUserProcessInstanceChangedEvent(Type type) {
        return allOf(
                instanceOf(UserProcessInstanceChangedEvent.class),
                isType(UserProcessInstanceChangedEvent.class, type)
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isUserProcessInstanceChangedEvent(Long userId, Type type) {
        return allOf(
                instanceOf(UserProcessInstanceChangedEvent.class),
                isType(UserProcessInstanceChangedEvent.class, type),
                value(UserProcessInstanceChangedEvent.class, UserProcessInstanceChangedEvent::getUserId, is(userId))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isUserTaskChangedEvent(Type type) {
        return allOf(
                instanceOf(UserTaskChangedEvent.class),
                isType(UserTaskChangedEvent.class, type)
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isUserTaskChangedEvent(Long userId, Type type) {
        return allOf(
                instanceOf(UserTaskChangedEvent.class),
                isType(UserTaskChangedEvent.class, type),
                value(UserTaskChangedEvent.class, UserTaskChangedEvent::getUserId, is(userId))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<T> isUserTaskChangedEvent(Long userId, String stateName, Type type) {
        return allOf(instanceOf(UserTaskChangedEvent.class),
                isType(UserTaskChangedEvent.class, type),
                value(UserTaskChangedEvent.class, UserTaskChangedEvent::getUserId, is(userId)),
                value(UserTaskChangedEvent.class, UserTaskChangedEvent::getSource, value(TaskInfo::getStateName, is(stateName)))
        );
    }

    public static <T extends EngineEvent<?>> Matcher<? super T> isProviderTaskChangedEvent(Type type) {
        return allOf(instanceOf(ProviderTaskChangedEvent.class),
                isType(ProviderTaskChangedEvent.class, type)
        );
    }

    private static <T extends EngineEvent<?>> Matcher<? super T> isType(Class<? extends EngineEvent<?>> eventClass, Type type) {
        return value(eventClass, EngineEvent::getType, is(type));
    }

    private EngineEventMatcher() {
    }
}
