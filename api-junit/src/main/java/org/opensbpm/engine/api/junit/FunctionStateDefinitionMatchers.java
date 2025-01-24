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

import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isObjectName;

import org.opensbpm.engine.api.model.definition.PermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.opensbpm.engine.api.junit.CommonMatchers.value;
import static org.opensbpm.engine.api.junit.StateDefinitionMatchers.isState;
import static org.opensbpm.engine.api.junit.CommonMatchers.isTypeWith;
import static org.hamcrest.Matchers.hasItem;
import static org.opensbpm.engine.api.junit.ProcessDefinitionMatchers.isAttributeName;

import org.opensbpm.engine.api.model.definition.PermissionDefinition.AttributePermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.NestedPermissionDefinition;

import static org.hamcrest.Matchers.allOf;

/**
 * Factory for {@link FunctionStateDefinition} specific {{@link Matcher}'s
 */
public final class FunctionStateDefinitionMatchers {

    public static Matcher<StateDefinition> isFunctionState(String stateName) {
        return isState(FunctionStateDefinition.class, stateName);
    }

    public static Matcher<StateDefinition> isFunctionState(String name, Matcher<StateDefinition>... matchers) {
        return isState(FunctionStateDefinition.class, name, matchers);
    }

    public static Matcher<StateDefinition> isFunctionState(String name, String displayName,
            Matcher<StateDefinition>... matchers) {
        return isState(FunctionStateDefinition.class, name, displayName, matchers);
    }

    /**
     * {@link org.hamcrest.Matchers#containsInAnyOrder} Matcher for {@link FunctionStateDefinition#getPermissions() }.
     *
     * @param permissions permissions to match
     * @return {@link Matcher} of type {@link StateDefinition}
     */
    public static Matcher<StateDefinition> containsPermisssions(Matcher<? super PermissionDefinition>... permissions) {
        return value(FunctionStateDefinition.class,
                FunctionStateDefinition::getPermissions,
                containsInAnyOrder(permissions)
        );
    }

    /**
     * {@link org.hamcrest.Matchers#containsInAnyOrder} Matcher for {@link FunctionStateDefinition#getHeads() () }.
     *
     * @param heads heads to match
     * @return {@link Matcher} of type {@link StateDefinition}
     */
    public static Matcher<StateDefinition> containsHeads(Matcher<? super StateDefinition>... heads) {
        return value(FunctionStateDefinition.class,
                FunctionStateDefinition::getHeads,
                containsInAnyOrder(heads)
        );
    }

    public static Matcher<PermissionDefinition> isPermission(String objectName, String fieldName, Permission permission,
            boolean mandatory) {
        return isTypeWith(PermissionDefinition.class,
                value(PermissionDefinition::getObjectDefinition, isObjectName(objectName)),
                value(PermissionDefinition::getAttributePermissions, hasItem(isFieldPermission(fieldName, permission, mandatory)))
        );
    }

    public static Matcher<PermissionDefinition> isPermission(String objectName,
            Matcher<? super AttributePermissionDefinition>... attributePermissions) {
        return isTypeWith(PermissionDefinition.class,
                value(PermissionDefinition::getObjectDefinition, isObjectName(objectName)),
                value(PermissionDefinition::getAttributePermissions, containsInAnyOrder(attributePermissions))
        );
    }

    public static Matcher<AttributePermissionDefinition> isFieldPermission(String fieldName, Permission permission,boolean mandatory) {
        return allOf(
                value(AttributePermissionDefinition::getAttribute, isAttributeName(fieldName)),
                value(AttributePermissionDefinition::getPermission, is(permission)),
                value(AttributePermissionDefinition::isMandatory, is(mandatory))
        );
        
    }
    public static Matcher<AttributePermissionDefinition> isFieldPermission(String fieldName, Permission permission,boolean mandatory,String defaultValue) {
        return allOf(
                value(AttributePermissionDefinition::getAttribute, isAttributeName(fieldName)),
                value(AttributePermissionDefinition::getPermission, is(permission)),
                value(AttributePermissionDefinition::isMandatory, is(mandatory)),
                value(attributePermission -> attributePermission.getDefaultValue().orElse(null), is(defaultValue))
        );
    }

    public static Matcher<AttributePermissionDefinition> isNestedPermission(String attributename,
            Matcher<? super AttributePermissionDefinition>... attributePermissions) {
        return allOf(
                value(NestedPermissionDefinition.class, NestedPermissionDefinition::getAttribute, isAttributeName(attributename)),
                value(NestedPermissionDefinition.class, NestedPermissionDefinition::getAttributePermissions, containsInAnyOrder(attributePermissions))
        );
    }

    private FunctionStateDefinitionMatchers() {
    }

}
