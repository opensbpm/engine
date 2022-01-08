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

import static org.opensbpm.engine.api.junit.CommonMatchers.value;

import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.FieldDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ReferenceDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToManyDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToOneDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public final class ProcessDefinitionMatchers {

    public static Matcher<SubjectDefinition> isSubjectName(String name) {
        return value(SubjectDefinition::getName, is(name));
    }

    public static Matcher<SubjectDefinition> isStarterSubjectName(String name) {
        return allOf(
                value(SubjectDefinition::getName, is(name)),
                value(SubjectDefinition::isStarter, is(true))
        );
    }

    public static Matcher<ObjectDefinition> isObject(String name, Matcher<? super AttributeDefinition>... matchers) {
        return allOf(
                isObjectName(name),
                value(ObjectDefinition::getAttributes, contains(matchers))
        );
    }

    public static Matcher<ObjectDefinition> isObject(String name, String displayName, Matcher<? super AttributeDefinition>... matchers) {
        return allOf(
                isObjectName(name),
                isObjectDisplayName(displayName),
                value(ObjectDefinition::getAttributes, contains(matchers))
        );
    }

    public static Matcher<ObjectDefinition> isObjectName(String name) {
        return value(ObjectDefinition::getName, is(name));
    }

    public static Matcher<ObjectDefinition> isObjectDisplayName(String displayName) {
        return value(ObjectDefinition::getDisplayName, is(displayName));
    }

    public static Matcher<AttributeDefinition> isField(String name, FieldType type) {
        return allOf(
                value(AttributeDefinition::getName, is(name)),
                value(FieldDefinition.class, FieldDefinition::getFieldType, is(type))
        );
    }

    public static Matcher<AttributeDefinition> isFieldWithIndex(String name, FieldType type) {
        return allOf(
                value(AttributeDefinition::getName, is(name)),
                value(FieldDefinition.class, FieldDefinition::getFieldType, is(type)),
                value(FieldDefinition.class, FieldDefinition::isIndexed, is(true))
        );
    }

    public static Matcher<AttributeDefinition> isReference(String name, String referenceObjectName) {
        return allOf(
                value(AttributeDefinition::getName, is(name)),
                value(ReferenceDefinition.class, ReferenceDefinition::getObjectDefinition, isObjectName(referenceObjectName))
        );
    }

    public static Matcher<AttributeDefinition> isToOne(String name, Matcher<? super AttributeDefinition>... matchers) {
        return allOf(
                value(AttributeDefinition::getName, is(name)),
                value(ToOneDefinition.class, ToOneDefinition::getAttributes, contains(matchers))
        );
    }

    public static Matcher<AttributeDefinition> isToMany(String name, Matcher<? super AttributeDefinition>... matchers) {
        return allOf(
                value(AttributeDefinition::getName, is(name)),
                value(ToManyDefinition.class, ToManyDefinition::getAttributes, contains(matchers))
        );
    }

    public static Matcher<AttributeDefinition> isAttributeName(String name) {
        return value(AttributeDefinition::getName, is(name));
    }

    private ProcessDefinitionMatchers() {
    }

}
