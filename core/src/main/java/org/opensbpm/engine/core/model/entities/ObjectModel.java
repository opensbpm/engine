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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity
@Table(name = "objectmodel", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"processmodel", "name"})
})
public class ObjectModel implements IsAttributeParent, HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 1, max = 100)
    protected String name;

    private String displayName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "objectModel")
    @OrderBy("position ASC")
    private List<AttributeModel> attributeModels;

    protected ObjectModel() {
    }

    public ObjectModel(String name) {
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

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Collection<AttributeModel> getAttributeModels() {
        return emptyOrUnmodifiableList(attributeModels);
    }

    public Stream<AttributeModel> getAllAttributeModels() {
        return Stream.concat(
                emptyOrUnmodifiableList(attributeModels).stream(),
                emptyOrUnmodifiableList(attributeModels).stream()
                        .map(attributeModel -> attributeModel.accept(new AttributeModelVisitor<IsAttributeParent>() {
                    @Override
                    public IsAttributeParent visitSimple(SimpleAttributeModel attributeModel) {
                        return null;
                    }

                    @Override
                    public IsAttributeParent visitNested(NestedAttributeModel attributeModel) {
                        return attributeModel;
                    }

                    @Override
                    public IsAttributeParent visitIndexed(IndexedAttributeModel attributeModel) {
                        return attributeModel;
                    }

                }))
                        .filter(Objects::nonNull)
                        .flatMap(nestedModel -> nestedModel.getAttributeModels().stream())
        );
    }

    public <T extends AttributeModel> T addAttributeModel(T attributeModel) {
        Objects.requireNonNull(attributeModel, "attributeModel must not be null");
        attributeModel.setPosition(getAttributeModels().size() + 1);
        attributeModels = lazyAdd(attributeModels, attributeModel);
        Collections.sort(attributeModels, (o1, o2) -> {
            return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
        });
        return attributeModel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", name)
                .append("attributes", getAttributeModels(), false)
                .toString();
    }

}
