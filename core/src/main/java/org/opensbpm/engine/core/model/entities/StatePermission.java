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
package org.opensbpm.engine.core.model.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity
@Table(name = "statepermission", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"functionstate", "attributemodel"}),
    @UniqueConstraint(columnNames = {"parent", "attributemodel"})
})
public class StatePermission implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "functionstate", updatable = false)
    private FunctionState functionState;

    @ManyToOne
    @JoinColumn(name = "parent", updatable = false)
    private StatePermission parent;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "attributemodel", nullable = false, updatable = false)
    private AttributeModel attributeModel;

    @Column(nullable = false)
    private Permission permission;

    @Column
    private boolean mandatory = false;

        /**
     * Script for determine the default value of the attribute
     */
    @Column
    private String defaultValue;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    private Set<StatePermission> childPermissions;

    protected StatePermission() {
    }

    public StatePermission(FunctionState functionState, AttributeModel attributeModel, Permission permission) {
        this.functionState = Objects.requireNonNull(functionState);
        this.attributeModel = Objects.requireNonNull(attributeModel);
        this.permission = Objects.requireNonNull(permission);
    }

    public StatePermission(StatePermission parent, AttributeModel attributeModel, Permission permission) {
        this.parent = Objects.requireNonNull(parent);
        this.attributeModel = Objects.requireNonNull(attributeModel);
        this.permission = Objects.requireNonNull(permission);
    }

    @Override
    public Long getId() {
        return id;
    }

    public FunctionState getFunctionState() {
        return functionState;
    }

    public StatePermission getParent() {
        return parent;
    }

    public AttributeModel getAttributeModel() {
        return attributeModel;
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Set<StatePermission> getChildPermissions() {
        return emptyOrUnmodifiableSet(childPermissions);
    }

    public Stream<StatePermission> getAllPermissions() {
        return Stream.concat(getChildPermissions().stream(),
                getChildPermissions().stream()
                        .flatMap(statePermission -> statePermission.getAllPermissions())
        );
    }

    public StatePermission addChildPermission(AttributeModel attributeModel, Permission permission) {
        StatePermission statePermission = new StatePermission(this, attributeModel, permission);
        childPermissions = lazyAdd(childPermissions, statePermission);
        return statePermission;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("attribute", attributeModel)
                .append("permission", permission)
                .toString();
    }

}
