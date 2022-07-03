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
package org.opensbpm.engine.api.model.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AttributeBuilder;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import org.opensbpm.engine.api.model.definition.ObjectDefinition.AttributeDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.AttributePermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.NestedPermissionDefinition;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.ToManyPermission;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.ToOnePermission;
import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.FunctionStateDefinition;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableMap;
import static org.opensbpm.engine.utils.StreamUtils.mapToList;

public class FunctionStateBuilder extends StateBuilder<FunctionStateBuilder, FunctionStateDefinition> {

    private String provider;
    private final Map<String, String> parameters = new LinkedHashMap<>();
    private final List<PermissionBuilder> permissionBuilders = new ArrayList<>();
    private final List<StateBuilder<?, ?>> heads = new ArrayList<>();

    public FunctionStateBuilder(String name) {
        super(name);
    }

    @Override
    protected FunctionStateBuilder self() {
        return this;
    }
    
    public FunctionStateBuilder withProvider(String provider) {
        checkBuilt();
        this.provider = provider;
        return this;
    }

    public FunctionStateBuilder addParameter(String name, String value) {
        checkBuilt();
        parameters.put(name, value);
        return this;
    }

    public FunctionStateBuilder addPermission(PermissionBuilder permissionBuilder) {
        checkBuilt();
        permissionBuilders.add(permissionBuilder);
        return this;
    }

    public FunctionStateBuilder toHead(StateBuilder<?, ?> headBuilder) {
        LOGGER.log(Level.FINER, "''{0}'' to head ''{1}''", new Object[]{name, headBuilder.name});
        checkBuilt();
        heads.add(headBuilder);
        return this;
    }

    @Override
    protected FunctionStateDefinition createState(String displayName, StateEventType eventType) {
        List<PermissionDefinition> permissionDefinitions = mapToList(permissionBuilders, AbstractBuilder::build);
        return new FunctionStateDefinitionImpl(name, displayName, eventType, permissionDefinitions);
    }

    @Override
    protected void collectInto(Set<StateBuilder<?, ?>> stateBuilders) {
        if (!stateBuilders.contains(this)) {
            stateBuilders.add(this);
            heads.forEach(builder -> builder.collectInto(stateBuilders));
        }
    }

    @Override
    protected void updateHeads() {
        ((FunctionStateDefinitionImpl) build()).setHeads(mapToList(heads, StateBuilder::build));
    }

    public static class PermissionBuilder extends AbstractBuilder<PermissionDefinition,PermissionBuilder> {

        private final ObjectBuilder objectBuilder;
        private final List<AbstractAttributePermissionBuilder<? extends AttributePermissionDefinition,?>> attributePermissions = new ArrayList<>();

        public PermissionBuilder(ObjectBuilder objectBuilder) {
            this.objectBuilder = objectBuilder;
        }

        @Override
        protected PermissionBuilder self() {
            return this;
        }
        

        public PermissionBuilder addReadPermission(AttributeBuilder<?,?> attributeBuilder) {
            return addPermission(attributeBuilder, Permission.READ, false);
        }

        public PermissionBuilder addWritePermission(AttributeBuilder<?,?> attributeBuilder, boolean mandatory) {
            return addPermission(attributeBuilder, Permission.WRITE, mandatory);
        }

        public PermissionBuilder addPermission(AttributeBuilder<?,?> attributeBuilder,
                Permission permission, boolean mandatory) {
            return addPermission(new AttributePermissionBuilder(attributeBuilder, permission, mandatory));
        }

        public PermissionBuilder addPermission(
                AbstractAttributePermissionBuilder<? extends AttributePermissionDefinition,?> permission) {
            return addPermissions(Arrays.asList(permission));
        }

        public PermissionBuilder addPermissions(
                List<AbstractAttributePermissionBuilder<? extends AttributePermissionDefinition,?>> permissions) {
            checkBuilt();
            attributePermissions.addAll(permissions);
            return this;
        }

        @Override
        protected PermissionDefinition create() {
            List<AttributePermissionDefinition> permissions = mapToList(attributePermissions,
                    permission -> permission.build());
            ObjectDefinition objectDefinition = objectBuilder.build();
            return new PermissionDefinition() {
                @Override
                public ObjectDefinition getObjectDefinition() {
                    return objectDefinition;
                }

                @Override
                public List<AttributePermissionDefinition> getAttributePermissions() {
                    return permissions;
                }

            };
        }
    }

    public abstract static class AbstractAttributePermissionBuilder<
            V extends AttributePermissionDefinition,
            T extends AbstractAttributePermissionBuilder<V,T>> 
            extends AbstractBuilder<V,T> {

        private final AttributeBuilder<?,?> attributeBuilder;
        private final Permission permission;
        private final boolean mandatory;

        protected AbstractAttributePermissionBuilder(AttributeBuilder<?,?> attributeBuilder,
                Permission permission, boolean mandatory) {
            this.attributeBuilder = attributeBuilder;
            this.permission = permission;
            this.mandatory = mandatory;
        }
        

        @Override
        protected V create() {
            AttributeDefinition attribute = attributeBuilder.build();
            return createAttributePermissionDefinition(attribute, permission, mandatory);
        }

        protected abstract V createAttributePermissionDefinition(AttributeDefinition attribute,
                Permission permission, boolean mandatory);

    }

    public static class AttributePermissionBuilder
            extends AbstractAttributePermissionBuilder<AttributePermissionDefinition,AttributePermissionBuilder> {

        public AttributePermissionBuilder(AttributeBuilder<?,?> attributeBuilder, Permission permission, boolean mandatory) {
            super(attributeBuilder, permission, mandatory);
        }

        @Override
        protected AttributePermissionBuilder self() {
            return this;
        }

        
        @Override
        protected AttributePermissionDefinition createAttributePermissionDefinition(AttributeDefinition attribute,
                Permission permission, boolean mandatory) {
            return new SimpleAttributePermissionDefinition(attribute, permission, mandatory);
        }
    }

    public abstract static class NestedAttributePermissionBuilder<
            V extends NestedPermissionDefinition,
            T extends NestedAttributePermissionBuilder<V,T>
            > extends AbstractAttributePermissionBuilder<V,T> {

        protected final List<AbstractAttributePermissionBuilder<? extends AttributePermissionDefinition,?>> permissionBuilders = new ArrayList<>();

        public NestedAttributePermissionBuilder(AttributeBuilder<?,?> attributeBuilder,
                Permission permission, boolean mandatory) {
            super(attributeBuilder, permission, mandatory);
        }

        public T addPermission(AbstractAttributePermissionBuilder<? extends AttributePermissionDefinition,?> permission) {
            return addPermissions(Arrays.asList(permission));
        }

        public T addPermissions(List<AbstractAttributePermissionBuilder<? extends AttributePermissionDefinition,?>> permissions) {
            checkBuilt();
            this.permissionBuilders.addAll(permissions);
            return (T) self();
        }

    }

    public static class ToOnePermissionBuilder extends NestedAttributePermissionBuilder<ToOnePermission,ToOnePermissionBuilder> {

        public ToOnePermissionBuilder(AttributeBuilder<?,?> attributeBuilder, Permission permission, boolean mandatory) {
            super(attributeBuilder, permission, mandatory);
        }

        @Override
        protected ToOnePermissionBuilder self() {
            return this;
        }

        
        @Override
        protected ToOnePermission createAttributePermissionDefinition(AttributeDefinition attribute,
                Permission permission, boolean mandatory) {
            List<AttributePermissionDefinition> permissions = mapToList(permissionBuilders, AbstractBuilder::build);
            return new ToOnePermissionImpl(attribute, permission, mandatory, permissions);
        }

    }

    public static class ToManyPermissionBuilder extends NestedAttributePermissionBuilder<ToManyPermission,ToManyPermissionBuilder> {

        public ToManyPermissionBuilder(AttributeBuilder<?,?> attributeBuilder,
                Permission permission, boolean mandatory) {
            super(attributeBuilder, permission, mandatory);
        }

        @Override
        protected ToManyPermissionBuilder self() {
            return this;
        }

        
        @Override
        protected ToManyPermission createAttributePermissionDefinition(AttributeDefinition attribute,
                Permission permission, boolean mandatory) {
            List<AttributePermissionDefinition> permissions = mapToList(permissionBuilders, AbstractBuilder::build);
            return new ToManyPermissionImpl(attribute, permission, mandatory, permissions);
        }

    }

    private class FunctionStateDefinitionImpl extends AbstractStateDefinition
            implements FunctionStateDefinition {

        private final List<PermissionDefinition> permissions;
        private List<StateDefinition> heads;

        public FunctionStateDefinitionImpl(String name, String displayName,
                StateEventType eventType, List<PermissionDefinition> permissions) {
            super(name, displayName, eventType);
            this.permissions = emptyOrUnmodifiableList(permissions);
        }

        @Override
        public String getProvider() {
            return provider;
        }

        @Override
        public Map<String, String> getParameters() {
            return emptyOrUnmodifiableMap(parameters);
        }

        @Override
        public List<PermissionDefinition> getPermissions() {
            return emptyOrUnmodifiableList(permissions);
        }

        @Override
        public List<StateDefinition> getHeads() {
            return emptyOrUnmodifiableList(heads);
        }

        public void setHeads(List<StateDefinition> heads) {
            this.heads = new ArrayList<>(heads);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("name", getName())
                    .append("heads", getHeads(), false)
                    .toString();
        }
    }

    private static class AbstractAttributePermissionDefinition {

        private final AttributeDefinition attribute;
        private final Permission permission;
        private final boolean mandatory;

        protected AbstractAttributePermissionDefinition(AttributeDefinition attribute, Permission permission, boolean mandatory) {
            this.attribute = attribute;
            this.permission = permission;
            this.mandatory = mandatory;
        }

        public AttributeDefinition getAttribute() {
            return attribute;
        }

        public Permission getPermission() {
            return permission;
        }

        public boolean isMandatory() {
            return mandatory;
        }

    }

    private static class SimpleAttributePermissionDefinition extends AbstractAttributePermissionDefinition
            implements AttributePermissionDefinition {

        public SimpleAttributePermissionDefinition(AttributeDefinition attribute, Permission permission, boolean mandatory) {
            super(attribute, permission, mandatory);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("attribute", getAttribute().getName())
                    .toString();
        }
    }

    private static abstract class AbstractNestedPermissionDefinition extends AbstractAttributePermissionDefinition
            implements NestedPermissionDefinition {

        private final List<AttributePermissionDefinition> permissions;

        public AbstractNestedPermissionDefinition(AttributeDefinition attribute, Permission permission, boolean mandatory, List<AttributePermissionDefinition> permissions) {
            super(attribute, permission, mandatory);
            this.permissions = emptyOrUnmodifiableList(permissions);
        }

        @Override
        public List<AttributePermissionDefinition> getAttributePermissions() {
            return emptyOrUnmodifiableList(permissions);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("attribute", getAttribute().getName())
                    .append("attributes", getAttributePermissions())
                    .toString();
        }
    }

    private static class ToOnePermissionImpl extends AbstractNestedPermissionDefinition implements ToOnePermission {

        public ToOnePermissionImpl(AttributeDefinition attribute, Permission permission, boolean mandatory, List<AttributePermissionDefinition> permissions) {
            super(attribute, permission, mandatory, permissions);
        }

    }

    private static class ToManyPermissionImpl extends AbstractNestedPermissionDefinition implements ToManyPermission {

        public ToManyPermissionImpl(AttributeDefinition attribute, Permission permission, boolean mandatory, List<AttributePermissionDefinition> permissions) {
            super(attribute, permission, mandatory, permissions);
        }

    }

}
