package org.opensbpm.engine.api.instance;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.FieldType;
import org.opensbpm.engine.api.model.definition.Occurs;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

@XmlAccessorType(XmlAccessType.FIELD)
public class NestedAttributeSchema extends AttributeSchema implements IsAttributesContainer {

    @XmlAttribute(required = true)
    private Occurs occurs;

    private List<AttributeSchema> attributes;

    public NestedAttributeSchema() {
        //JAXB constructor
    }

    public NestedAttributeSchema(Long id, String name, Occurs occurs, List<AttributeSchema> attributes) {
        super(id, name, occurs == Occurs.ONE ? FieldType.NESTED : FieldType.LIST);
        this.occurs = occurs;
        this.attributes = new ArrayList<>(attributes);
    }

    public Occurs getOccurs() {
        return occurs;
    }

    public List<AttributeSchema> getAttributes() {
        return emptyOrUnmodifiableList(attributes);
    }

    public <T> T accept(AttributeSchemaVisitor<T> visitor) {
        if (Occurs.ONE == getOccurs()) {
            return visitor.visitNested(this);
        } else if (Occurs.UNBOUND == getOccurs()) {
            return visitor.visitIndexed(this);
        } else {
            throw new UnsupportedOperationException(getOccurs() + " not implemented yet");
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("occures", occurs)
                .append("attributes", attributes)
                .toString();
    }

}
