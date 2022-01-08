package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectSchema implements Serializable, IsAttributesContainer {

    public static ObjectSchema of(Long id, String name, List<AttributeSchema> attributes) {
        ObjectSchema objectSchema = new ObjectSchema();
        objectSchema.id = Objects.requireNonNull(id, "id must be nono null");
        objectSchema.name = Objects.requireNonNull(name, "name must be nono null");
        objectSchema.attributes = new ArrayList<>(attributes);
        return objectSchema;
    }

    @XmlElement(required = true)
    private Long id;

    @XmlElement(required = true)
    private String name;

    @XmlElements({
        @XmlElement(name = "field", type = AttributeSchema.class),
        @XmlElement(name = "nested", type = NestedAttributeSchema.class)
    })
    private List<AttributeSchema> attributes;

    public ObjectSchema() {
        //JAXB constructor
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<AttributeSchema> getAttributes() {
        return emptyOrUnmodifiableList(attributes);
    }

    public Optional<AttributeSchema> getAttribute(Predicate<AttributeSchema> filter) {
        return getAttributes(filter).findFirst();
    }

    public Optional<AttributeSchema> getIdAttribute() {
        return getAttribute(AttributeSchema::isIdSchema);
    }

    public Stream<AttributeSchema> getAttributes(Predicate<AttributeSchema> filter) {
        return attributes.stream().filter(filter);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("attributes", attributes)
                .toString();
    }
}
