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
package org.opensbpm.engine.api.junit;

import java.io.Serializable;
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

/**
 * Hamcrest-{@link Matcher}'s for {@link EngineEvent} implementations.
 * For example:
 * <pre>assertThat(RolceChangedEvent, isRoleChangedEvent("Role Name", Type.UPDATE));</pre>
 */
public final class EngineEventMatcher {

    /**
     * creates a matcher that matches when the examined object is a {@link RoleChangedEvent} with the given roleName and type.
     * For example:
     * <pre>assertThat(new RoleChangedEvent(RoleToken.of(1l,"foo"),Type.CREATE), isRoleChangedEvent("foo", Type.CREATE));</pre>
     *
     * @param roleName name of RoleToken (see {@link RoleToken#name})
     * @param type type of RoleChangedEvent (see {@link RoleChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isRoleChangedEvent(String roleName, Type type) {
        return allOf(instanceOf(RoleChangedEvent.class),
                isType(RoleChangedEvent.class, type),
                value(RoleChangedEvent.class, RoleChangedEvent::getSource, value("name", RoleToken::getName, is(roleName)))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link RoleUserChangedEvent} with the given userId and type.
     * For example:
     * <pre>assertThat(new RoleUserChangedEvent(UserToken.of(1l,"foo"),Type.CREATE), isRoleUserChangedEvent(1l Type.CREATE));</pre>
     *
     * @param userId id of UserToken (see {@link UserToken#id})
     * @param type type of RoleUserChangedEvent (see {@link RoleUserChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isRoleUserChangedEvent(Long userId, Type type) {
        return allOf(instanceOf(RoleUserChangedEvent.class),
                isType(RoleUserChangedEvent.class, type),
                value(RoleUserChangedEvent.class, RoleUserChangedEvent::getSource, value(UserToken::getId, is(userId)))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserChangedEvent} with the given userName and type.
     * For example:
     * <pre>assertThat(new UserChangedEvent(UserToken.of(1l,"foo"),Type.CREATE), isUserChangedEvent("foo", Type.CREATE));</pre>
     *
     * @param userName name of UerToken (see {@link UserToken#name})
     * @param type type of UserChangedEvent (see {@link UserChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserChangedEvent(String userName, Type type) {
        return allOf(instanceOf(UserChangedEvent.class),
                isType(UserChangedEvent.class, type),
                value(UserChangedEvent.class, UserChangedEvent::getSource, value("name", UserToken::getName, is(userName)))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserChangedEvent} with the given userId and type.
     * For example:
     * <pre>assertThat(new UserChangedEvent(UserToken.of(1l,"foo"),Type.CREATE), isUserChangedEvent(1l, Type.CREATE));</pre>
     *
     * @param userId id of UerToken (see {@link UserToken#id})
     * @param type type of UserChangedEvent (see {@link UserChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserChangedEvent(Long userId, Type type) {
        return allOf(instanceOf(UserChangedEvent.class),
                isType(UserChangedEvent.class, type),
                value(UserChangedEvent.class, UserChangedEvent::getSource, value(UserToken::getId, is(userId)))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link ProcessModelChangedEvent} with the given modelName and type.
     * For example:
     * <pre>assertThat(new ProcessModelChangedEvent(ProcessModelInfo,Type.CREATE), isProcessModelChangedEvent(1l, Type.CREATE));</pre>
     *
     * @param modelName name of ProcessModelInfo (see {@link ProcessModelInfo#name})
     * @param type type of ProcessModelChangedEvent (see {@link ProcessModelChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isProcessModelChangedEvent(String modelName, Type type) {
        return allOf(
                instanceOf(ProcessModelChangedEvent.class),
                isType(ProcessModelChangedEvent.class, type),
                value(ProcessModelChangedEvent.class, ProcessModelChangedEvent::getSource, value(ProcessModelInfo::getName, is(modelName)))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserProcessModelChangedEvent} with the given userId.
     * For example:
     * <pre>assertThat(new UserProcessModelChangedEvent(1l,ProcessModelInfo,Type.CREATE), isUserProcessModelChangedEvent(1l));</pre>
     *
     * @param userId id of UserProcessModelChangedEvent (see {@link UserProcessModelChangedEvent#userId})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserProcessModelChangedEvent(Long userId) {
        return allOf(
                instanceOf(UserProcessModelChangedEvent.class),
                value(UserProcessModelChangedEvent.class, UserProcessModelChangedEvent::getUserId, is(userId))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserProcessModelChangedEvent} with the given type.
     * For example:
     * <pre>assertThat(new UserProcessModelChangedEvent(1l,ProcessModelInfo,Type.CREATE), isUserProcessModelChangedEvent(Type.CREATE));</pre>
     *
     * @param type type of UserProcessModelChangedEvent (see {@link UserProcessModelChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserProcessModelChangedEvent(Type type) {
        return allOf(
                instanceOf(UserProcessModelChangedEvent.class),
                isType(UserProcessModelChangedEvent.class, type)
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserProcessModelChangedEvent} with the given userId and type.
     * For example:
     * <pre>assertThat(new UserProcessModelChangedEvent(1l,ProcessModelInfo,Type.CREATE), isUserProcessModelChangedEvent(1l, Type.CREATE));</pre>
     *
     * @param userId id of UserProcessModelChangedEvent (see {@link UserProcessModelChangedEvent#userId})
     * @param type type of UserProcessModelChangedEvent (see {@link UserProcessModelChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserProcessModelChangedEvent(Long userId, Type type) {
        return allOf(
                instanceOf(UserProcessModelChangedEvent.class),
                value(UserProcessModelChangedEvent.class, UserProcessModelChangedEvent::getUserId, is(userId)),
                isType(UserProcessModelChangedEvent.class, type)
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link ProcessInstanceChangedEvent} with the given type.
     * For example:
     * <pre>assertThat(new ProcessInstanceChangedEvent(ProcessInfo,Type.CREATE), isProcessInstanceChangedEvent(Type.CREATE));</pre>
     *
     * @param type type of ProcessInstanceChangedEvent (see {@link ProcessInstanceChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isProcessInstanceChangedEvent(Type type) {
        return allOf(
                instanceOf(ProcessInstanceChangedEvent.class),
                isType(ProcessInstanceChangedEvent.class, type)
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserProcessInstanceChangedEvent} with the given type.
     * For example:
     * <pre>assertThat(new UserProcessInstanceChangedEvent(1l,ProcessInfo,Type.CREATE), isUserProcessInstanceChangedEvent(Type.CREATE));</pre>
     *
     * @param type type of UserProcessInstanceChangedEvent (see {@link UserProcessInstanceChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserProcessInstanceChangedEvent(Type type) {
        return allOf(
                instanceOf(UserProcessInstanceChangedEvent.class),
                isType(UserProcessInstanceChangedEvent.class, type)
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserProcessInstanceChangedEvent} with the given userId and type.
     * For example:
     * <pre>assertThat(new UserProcessInstanceChangedEvent(1l,ProcessInfo,Type.CREATE), isUserProcessInstanceChangedEvent(1l, Type.CREATE));</pre>
     *
     * @param userId id of UserProcessInstanceChangedEvent (see {@link UserProcessInstanceChangedEvent#userId})
     * @param type type of UserProcessInstanceChangedEvent (see {@link UserProcessInstanceChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserProcessInstanceChangedEvent(Long userId, Type type) {
        return allOf(
                instanceOf(UserProcessInstanceChangedEvent.class),
                isType(UserProcessInstanceChangedEvent.class, type),
                value(UserProcessInstanceChangedEvent.class, UserProcessInstanceChangedEvent::getUserId, is(userId))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserTaskChangedEvent} with the given userId and type.
     * For example:
     * <pre>assertThat(new UserTaskChangedEvent(1l,TaskInfo,Type.CREATE), isUserTaskChangedEvent(1l, Type.CREATE));</pre>
     *
     * @param userId id of UserTaskChangedEvent (see {@link UserTaskChangedEvent#userId})
     * @param type type of UserTaskChangedEvent (see {@link UserTaskChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserTaskChangedEvent(Long userId, Type type) {
        return allOf(
                instanceOf(UserTaskChangedEvent.class),
                isType(UserTaskChangedEvent.class, type),
                value(UserTaskChangedEvent.class, UserTaskChangedEvent::getUserId, is(userId))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link UserTaskChangedEvent} with the given userId, stateName and type.
     * For example:
     * <pre>assertThat(new UserTaskChangedEvent(1l,TaskInfo(..."foo"...),Type.CREATE), isUserTaskChangedEvent(1l, "foo", Type.CREATE));</pre>
     *
     * @param userId id of UserTaskChangedEvent (see {@link UserTaskChangedEvent#userId})
     * @param stateName id of UserTaskChangedEvent (see {@link TaskInfo#stateName})
     * @param type type of UserTaskChangedEvent (see {@link UserTaskChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isUserTaskChangedEvent(Long userId, String stateName, Type type) {
        return allOf(instanceOf(UserTaskChangedEvent.class),
                isType(UserTaskChangedEvent.class, type),
                value(UserTaskChangedEvent.class, UserTaskChangedEvent::getUserId, is(userId)),
                value(UserTaskChangedEvent.class, UserTaskChangedEvent::getSource, value(TaskInfo::getStateName, is(stateName)))
        );
    }

    /**
     * creates a matcher that matches when the examined object is a {@link ProviderTaskChangedEvent} with the given type.
     * For example:
     * <pre>assertThat(new ProviderTaskChangedEvent(TaskInfo(..."foo"...),Type.CREATE), isProviderTaskChangedEvent(Type.CREATE));</pre>
     *
     * @param type type of ProviderTaskChangedEvent (see {@link ProviderTaskChangedEvent#getType()})
     * @return a {@link Matcher} with given parameters
     */
    public static <T extends EngineEvent<? extends Serializable>> Matcher<T> isProviderTaskChangedEvent(Type type) {
        return allOf(instanceOf(ProviderTaskChangedEvent.class),
                isType(ProviderTaskChangedEvent.class, type)
        );
    }

    private static <T extends EngineEvent<? extends Serializable>> Matcher<T> isType(Class<? extends EngineEvent<?>> eventClass, Type type) {
        return value(eventClass, EngineEvent::getType, is(type));
    }

    private EngineEventMatcher() {
    }
}
