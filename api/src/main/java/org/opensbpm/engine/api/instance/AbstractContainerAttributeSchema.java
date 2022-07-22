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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractContainerAttributeSchema extends AbstractAttributeSchema implements IsAttributesContainer {

    @XmlElements({
        @XmlElement(name = "field", type = SimpleAttributeSchema.class),
        @XmlElement(name = "nested", type = NestedAttributeSchema.class),
        @XmlElement(name = "indexed", type = IndexedAttributeSchema.class)
    })
    private List<AttributeSchema> attributes;

    public AbstractContainerAttributeSchema() {
        //JAXB constructor
    }

    protected AbstractContainerAttributeSchema(Long id,
            String name,
            List<AttributeSchema> attributes) {
        super(id, name);
        this.attributes = new ArrayList<>(attributes);

    }

    @Override
    public List<AttributeSchema> getAttributes() {
        return emptyOrUnmodifiableList(attributes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("attributes", attributes)
                .toString();
    }

}
