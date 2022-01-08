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

import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableMap;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;
import static org.opensbpm.engine.utils.StreamUtils.filterToOne;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;
import org.opensbpm.engine.api.model.definition.PermissionDefinition.Permission;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name = "statefunction")
public class FunctionState extends State implements Serializable {

    @Column(name = "provider")
    private String providerName;

    @ElementCollection
    @MapKeyColumn(name = "parameter")
    @Lob
    @Column(name = "value")
    @CollectionTable(name = "statefunction_parameters",
            joinColumns = @JoinColumn(name = "id", updatable = false))
    private Map<String, String> parameters = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "functionState")
    private Set<StatePermission> statePermissions;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "functionvertex",
            joinColumns = {
                @JoinColumn(name = "function",
                        referencedColumnName = "id",
                        nullable = false,
                        updatable = false)
            },
            inverseJoinColumns = {
                @JoinColumn(name = "head",
                        referencedColumnName = "id",
                        nullable = false,
                        updatable = false)
            },
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"function", "head"})
    )
    private Set<State> heads;

    protected FunctionState() {
    }

    public FunctionState(String name) {
        super(name);
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Map<String, String> getParameters() {
        return emptyOrUnmodifiableMap(parameters);
    }

    public void putParameter(String name, String value) {
        parameters.put(name, value);
    }

    public Collection<StatePermission> getStatePermissions() {
        return emptyOrUnmodifiableSet(statePermissions);
    }

    public StatePermission addStatePermission(AttributeModel attributeModel, Permission permission) {
        StatePermission statePermission = new StatePermission(this, attributeModel, permission);
        addStatePermission(statePermission);
        return statePermission;
    }

    public void addStatePermission(StatePermission statePermission) {
        statePermissions = lazyAdd(statePermissions, statePermission);
    }

    public boolean hasAnyStatePermission(ObjectModel objectModel) {
        return objectModel.getAllAttributeModels()
                .anyMatch(attributeModel -> hasAnyPermission(attributeModel));
    }

    public boolean hasAnyPermission(AttributeModel attributeModel) {
        return findStatePermission(attributeModel).isPresent();
    }

    public boolean hasReadPermission(AttributeModel attributeModel) {
        return hasPermission(attributeModel, Permission.READ);
    }

    public boolean hasWritePermission(AttributeModel attributeModel) {
        return hasPermission(attributeModel, Permission.WRITE);
    }

    private boolean hasPermission(AttributeModel attributeModel, Permission permission) {
        return findStatePermission(attributeModel)
                .map(statePermission -> statePermission.getPermission() == permission)
                .orElse(false);
    }

    public boolean isMandatory(AttributeModel attributeModel) {
        return findStatePermission(attributeModel)
                .map(statePermission -> statePermission.isMandatory())
                .orElse(false);
    }

    private Optional<StatePermission> findStatePermission(AttributeModel attributeModel) {
        Stream<StatePermission> statePermissions = Stream.concat(getStatePermissions().stream(),
                getStatePermissions().stream()
                        .flatMap(statePermission -> statePermission.getAllPermissions()));
        return filterToOne(statePermissions, statePermission
                -> statePermission.getAttributeModel().equalsId(attributeModel));
    }

    public void addHead(State head) {
        heads = lazyAdd(heads, head);
    }

    @Override
    public <T> T accept(StateVisitor<T> visitor) {
        return visitor.visitFunctionState(this);
    }

    @Override
    public Collection<State> getHeads() {
        return emptyOrUnmodifiableSet(heads);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("eventType", getEventType())
                .append("providerName", getProviderName())
                .toString();
    }

}
