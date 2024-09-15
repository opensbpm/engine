/** *****************************************************************************
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
 * ****************************************************************************
 */
package org.opensbpm.engine.core.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.core.utils.LocalDateTimeAttributeConverter;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.core.model.entities.SubjectModelVisitor.userSubjectModel;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity(name = "process_model")
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "major", "minor"})
})
public class ProcessModel implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Embedded
    private ModelVersion version;

    @Column
    @Lob
    private String description;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessModelState state = ProcessModelState.ACTIVE;

    @Column(nullable = false)
    @NotNull
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "processmodel", nullable = false, updatable = false)
    private Set<SubjectModel> subjectModels;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "processmodel", nullable = false, updatable = false)
    @OrderBy
    private List<ObjectModel> objectModels;

    protected ProcessModel() {
    }

    public ProcessModel(String name, ModelVersion version) {
        this.name = Objects.requireNonNull(name);
        this.version = Objects.requireNonNull(version);
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ModelVersion getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProcessModelState getState() {
        return state;
    }

    public void setState(ProcessModelState state) {
        this.state = Objects.requireNonNull(state);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SubjectModel getStarterSubjectModel() {
        return getSubjectModels().stream()
                .filter(SubjectModel::isStarter)
                .findFirst().orElse(null);
        //TODO
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException(toString() + " doesn't have a starter subject"));
    }

    public void setStarterSubject(SubjectModel starterSubject) {
        starterSubject.setStarter(true);
    }

    public boolean isStarterSubjectModel(SubjectModel subjectModel) {
        return getStarterSubjectModel().equals(subjectModel);
    }

    public Collection<SubjectModel> getSubjectModels() {
        return emptyOrUnmodifiableSet(subjectModels);
    }

    public Collection<UserSubjectModel> getUserSubjectModels() {
        return getSubjectModels().stream()
                .map(subjectModel -> subjectModel.accept(userSubjectModel()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public UserSubjectModel addUserSubjectModel(String name, List<Role> roles) {
        //PENDING Objects.requireNonNull(name, "name must not be null");
        return addSubjectModel(new UserSubjectModel(name, roles));
    }

    public ServiceSubjectModel addServiceSubjectModel(String name) {
        //PENDING Objects.requireNonNull(name, "name must not be null");
        return addSubjectModel(new ServiceSubjectModel(name));
    }

    public <T extends SubjectModel> T addSubjectModel(T subjectModel) {
        //TODO this method should/could be private
        subjectModels = lazyAdd(subjectModels, subjectModel);
        return subjectModel;
    }

    public Collection<ObjectModel> getObjectModels() {
        return emptyOrUnmodifiableList(objectModels);
    }

    /**
     * create a new {@link ObjectModel} with the given name and add it to this ProcessModel
     *
     * @param name name of {@link ObjectModel}, see {@link ObjectModel#name}
     * @return
     */
    public ObjectModel addObjectModel(String name) {
        Objects.requireNonNull(name, "name must not be null");
        ObjectModel objectModel = new ObjectModel(name);
        addObjectModel(objectModel);
        return objectModel;
    }

    public void addObjectModel(ObjectModel objectModel) {
        Objects.requireNonNull(objectModel, "objectModel must not be null");
        objectModels = lazyAdd(objectModels, objectModel);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", name)
                .toString();
    }

}
