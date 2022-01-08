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

import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.AttributePermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.PermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.ToManyPermissionBuilder;
import org.opensbpm.engine.api.model.builder.FunctionStateBuilder.ToOnePermissionBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AttributeBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.FieldBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ReferenceBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToManyBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.ToOneBuilder;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.FieldDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToManyDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.ToOneDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.ReceiveStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.SendStateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.ServiceSubjectDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.UserSubjectDefinition;
import java.util.Arrays;
import java.util.List;

/**
 * (Sugar)-Factory to create all kind of process-definition builders
 *
 */
public final class DefinitionFactory {

    /**
     * create a new {@link ProcessBuilder}
     *
     * @param name name of the {@link ProcessDefinition}
     * @return a new instance of {@link ProcessBuilder}
     */
    public static ProcessBuilder process(String name) {
        return new ProcessBuilder(name);
    }

    /**
     * create a new {@link UserSubjectBuilder}
     *
     * @param name name of the {@link UserSubjectDefinition }
     * @param roles roles for {@link UserSubjectDefinition }
     * @return a new instance of {@link UserSubjectBuilder}
     */
    public static UserSubjectBuilder userSubject(String name, String... roles) {
        return userSubject(name, Arrays.asList(roles));
    }

    /**
     * create a new {@link UserSubjectBuilder}
     *
     * @param name name of the {@link UserSubjectDefinition }
     * @param roles roles for {@link UserSubjectDefinition }
     * @return a new instance of {@link UserSubjectBuilder}
     */
    public static UserSubjectBuilder userSubject(String name, List<String> roles) {
        return new UserSubjectBuilder(name, roles);
    }

    /**
     * create a new {@link ServiceSubjectBuilder}
     *
     * @param name name of the {@link ServiceSubjectDefinition}
     * @return a new instance of {@link ServiceSubjectBuilder}
     */
    public static ServiceSubjectBuilder serviceSubject(String name) {
        return new ServiceSubjectBuilder(name);
    }

    /**
     * create a new {@link FunctionStateBuilder}
     *
     * @param name name of the {@link FunctionStateDefinition}
     * @return a new instance of {@link FunctionStateBuilder}
     */
    public static FunctionStateBuilder functionState(String name) {
        return new FunctionStateBuilder(name);
    }

    /**
     * create a new {@link PermissionBuilder}
     *
     * @return a new instance of {@link PermissionBuilder}
     */
    public static PermissionBuilder permission(ObjectBuilder object) {
        return new PermissionBuilder(object);
    }

    /**
     * create a new {@link AttributePermissionBuilder}
     *
     * @return a new instance of {@link AttributePermissionBuilder}
     */
    public static AttributePermissionBuilder simplePermission(
            AttributeBuilder<?> attributeBuilder, Permission permission, boolean mandatory){
        return new AttributePermissionBuilder(attributeBuilder, permission, mandatory);
    }
    
    public static ToOnePermissionBuilder toOnePermission(
            AttributeBuilder<?> attributeBuilder, Permission permission, boolean mandatory){
        return new ToOnePermissionBuilder(attributeBuilder, permission, mandatory);
    }
    
    public static ToManyPermissionBuilder toManyPermission(
            AttributeBuilder<?> attributeBuilder, Permission permission, boolean mandatory){
        return new ToManyPermissionBuilder(attributeBuilder, permission, mandatory);
    }
    
    /**
     * create a new {@link ReceiveStateBuilder}
     *
     * @param name name of the {@link ReceiveStateDefinition}
     * @return a new instance of {@link ReceiveStateBuilder}
     */
    public static ReceiveStateBuilder receiveState(String name) {
        return new ReceiveStateBuilder(name);
    }

    /**
     * create a new {@link SendStateBuilder}
     *
     * @param name name of the {@link SendStateDefinition}
     * @return a new instance of {@link SendStateBuilder}
     */
    public static SendStateBuilder sendState(String name, SubjectBuilder receiver, ObjectBuilder objectModel) {
        return new SendStateBuilder(name, receiver, objectModel);
    }

    /**
     * create a new {@link ObjectBuilder}
     *
     * @param name name of the {@link ObjectDefinition}
     * @return a new instance of {@link ObjectBuilder}
     */
    public static ObjectBuilder object(String name) {
        return new ObjectBuilder(name);
    }

    /**
     * create a new {@link FieldBuilder}
     *
     * @param name name of the {@link FieldDefinition}
     * @return a new instance of {@link FieldBuilder}
     */
    public static FieldBuilder field(String name, FieldType fieldType) {
        return new FieldBuilder(name, fieldType);
    }

    /**
     * create a new {@link ToOneBuilder}
     *
     * @param name name of the {@link ToOneDefinition}
     * @return a new instance of {@link ToOneBuilder}
     */
    public static ReferenceBuilder reference(String name,ObjectBuilder objectBuilder) {
        return new ReferenceBuilder(name,objectBuilder);
    }

    /**
     * create a new {@link ToOneBuilder}
     *
     * @param name name of the {@link ToOneDefinition}
     * @return a new instance of {@link ToOneBuilder}
     */
    public static ToOneBuilder toOne(String name) {
        return new ToOneBuilder(name);
    }

    /**
     * create a new {@link ToManyBuilder}
     *
     * @param name name of the {@link ToManyDefinition}
     * @return a new instance of {@link ToManyBuilder}
     */
    public static ToManyBuilder toMany(String name) {
        return new ToManyBuilder(name);
    }

    private DefinitionFactory() {
    }

}
