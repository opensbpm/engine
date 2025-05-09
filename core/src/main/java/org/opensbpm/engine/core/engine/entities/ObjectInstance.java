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
package org.opensbpm.engine.core.engine.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableMap;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"object_model", "process_instance"})
})
public class ObjectInstance implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsExclude
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "object_model", nullable = false, updatable = false)
    private ObjectModel objectModel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "process_instance", nullable = false, updatable = false)
    private ProcessInstance processInstance;

    @Lob
//    @Basic(fetch = LAZY)
    @Column(name = "virtue")
    @EqualsExclude
    private HashMap<Long, Serializable> value = new HashMap<Long, Serializable>();

    protected ObjectInstance() {
    }

    /*default*/ ObjectInstance(ObjectModel objectModel, ProcessInstance processInstance) {
        this.processInstance = Objects.requireNonNull(processInstance, "ProcessInstance must not be null");
        this.objectModel = Objects.requireNonNull(objectModel, "ObjectModel must not be null");
    }

    @Override
    public Long getId() {
        return id;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public Map<Long, Serializable> getValue() {
        return emptyOrUnmodifiableMap(value);
    }

    public void setValue(Map<Long, Serializable> value) {
        this.value = new HashMap<>(value);
    }    

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("objectmodel", objectModel)
                .toString();
    }

}
