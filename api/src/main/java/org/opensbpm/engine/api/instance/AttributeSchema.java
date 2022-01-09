package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.FieldType;

@XmlAccessorType(XmlAccessType.FIELD)
public class AttributeSchema implements Serializable {

    public static final String ID_NAME = "Id";

    public static AttributeSchema of(long id, String name, FieldType fieldType) {
        AttributeSchema attributeSchema = new AttributeSchema(id, name, fieldType);
        attributeSchema.id = Objects.requireNonNull(id, "id must not be null");
        attributeSchema.name = Objects.requireNonNull(name, "name must not be null");
        attributeSchema.fieldType = Objects.requireNonNull(fieldType, "fieldType must not be null");
        return attributeSchema;
    }

    @XmlElement(required = true)
    private Long id;

    @XmlElement(required = true)
    private String name;

    @XmlAttribute(required = true)
    private FieldType fieldType;

    @XmlAttribute
    private boolean required;

    @XmlAttribute
    private boolean readonly;

    @XmlAttribute
    private boolean indexed;

    //TODO @XmlAttribute
    @XmlTransient
    private ObjectSchema autocompleteReference;

    protected AttributeSchema() {
        //JAXB constructor
    }

    public AttributeSchema(Long id, String name, FieldType fieldType) {
        this.id = id;
        this.name = name;
        this.fieldType = fieldType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * returns the java-type of the field.
     *
     * @return
     * @see FieldType
     */
    public Class<?> getType() {
        return fieldType.getType();
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public Optional<ObjectSchema> getAutocompleteReference() {
        return Optional.ofNullable(autocompleteReference);
    }

    public void setAutocompleteReference(ObjectSchema autocompleteReference) {
        this.autocompleteReference = autocompleteReference;
    }

    public <T> T accept(AttributeSchemaVisitor<T> visitor) {
        return visitor.visitSimple(this);
    }

    public boolean isIdSchema() {
        return ID_NAME.equals(name);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", getName())
                .append("type", fieldType)
                .toString();
    }

}
