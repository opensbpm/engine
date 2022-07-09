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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.engine.AttributeStore;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableMap;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"object_model", "processinstance"})
})
public class ObjectInstance implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "object_model")
    private ObjectModel objectModel;

    @ManyToOne
    @JoinColumn
    private ProcessInstance processInstance;

    @Lob
//    @Basic(fetch = LAZY)
    @Column
    private HashMap<Long, Serializable> value = new HashMap<Long, Serializable>();

    protected ObjectInstance() {
    }

    public ObjectInstance(ObjectModel objectModel, ProcessInstance processInstance) {
        setProcessInstance(processInstance);
        this.objectModel = objectModel;
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

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        if (processInstance != null && !processInstance.getObjectInstances().contains(this)) {
            processInstance.addObjectInstance(this);
        }
    }

    public Map<Long, Serializable> getValue() {
        return emptyOrUnmodifiableMap(value);
    }

    public void setValue(Map<Long, Serializable> value) {
        this.value = new HashMap<>(value);
    }    
    
    public AttributeStore getAttributeStore() {
        return new AttributeStore(objectModel, value);
    }

    public void updateValues(FunctionState state, Map<Long, Serializable> data) {
        getAttributeStore().updateValues(state, data);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("objectmodel", objectModel)
                .toString();
    }

}
