/** *****************************************************************************
 * Copyright (C) 2022 Stefan Sedelmaier
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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.adapters.SerializableAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractAttributeSchema implements AttributeSchema {

    @XmlElement(required = true)
    private Long id;

    @XmlElement(required = true)
    private String name;

    @XmlAttribute
    private boolean required;

    @XmlAttribute
    private boolean readonly;

    @XmlAttribute
    private boolean indexed;

    @XmlJavaTypeAdapter(SerializableAdapter.class)
    @XmlElement
    private Serializable defaultValue;

    @XmlElement
    private Options options;

    protected AbstractAttributeSchema() {
        //JAXB constructor
    }

    protected AbstractAttributeSchema(Long id, String name) {
        this.id = Objects.requireNonNull(id, "id must be non null");
        this.name = Objects.requireNonNull(name, "name must be non null");
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    @Override
    public Optional<Serializable> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void setDefaultValue(Serializable defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Optional<Options> getOptions() {
        return Optional.ofNullable(options);
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .toString();
    }

}
