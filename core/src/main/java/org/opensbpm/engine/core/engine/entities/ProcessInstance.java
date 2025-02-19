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
 *****************************************************************************
 */
package org.opensbpm.engine.core.engine.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.utils.LocalDateTimeAttributeConverter;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.core.engine.entities.SubjectVisitor.userSubject;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;
import static org.opensbpm.engine.utils.StreamUtils.filterToOne;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;
import static org.opensbpm.engine.utils.StreamUtils.toOne;

@Entity(name = "processinstance")
public class ProcessInstance implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /* no cascade, ProcessModel MUST be saved before */
    @ManyToOne(optional = false)
    @JoinColumn(name = "processmodel")
    @NotNull
    private ProcessModel processModel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "startuser")
    private User startUser;

    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessInstanceState state;

    @NotNull
    @Column
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime startTime;

    private String cancelMessage;

    @Column
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "processInstance", cascade = CascadeType.ALL)
    private List<Subject> subjects;

    @OneToMany(mappedBy="processInstance", cascade = CascadeType.ALL)
    private Set<ObjectInstance> objectInstances;

    protected ProcessInstance() {
    }

    public ProcessInstance(ProcessModel processModel, User startUser) {
        this.processModel = Objects.requireNonNull(processModel);
        this.startUser = Objects.requireNonNull(startUser);
        this.state = ProcessInstanceState.ACTIVE;
        this.startTime = LocalDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public User getStartUser() {
        return startUser;
    }

    public ProcessInstanceState getState() {
        return state;
    }

    public void setState(final ProcessInstanceState state) {
        Objects.requireNonNull(state);
        this.state = state;

        if (ProcessInstanceState.CANCELLED_BY_SYSTEM == state
                || ProcessInstanceState.CANCELLED_BY_USER == state
                || ProcessInstanceState.FINISHED == state) {
            endTime = LocalDateTime.now();
        }
    }

    public boolean isStopped() {
        return ProcessInstanceState.FINISHED == state
                || ProcessInstanceState.CANCELLED_BY_SYSTEM == state
                || ProcessInstanceState.CANCELLED_BY_USER == state;
    }

    public boolean isActive() {
        return ProcessInstanceState.ACTIVE == state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getCancelMessage() {
        return cancelMessage;
    }

    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = Objects.requireNonNull(cancelMessage);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Collection<Subject> getSubjects() {
        return emptyOrUnmodifiableList(subjects);
    }

    public Collection<UserSubject> getUserSubjects() {
        return getSubjects().stream()
                .map(subject -> subject.accept(userSubject()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void addSubject(Subject subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        if (!getSubjects().contains(subject)) {
            subjects = lazyAdd(subjects, subject);
        }
        if (subject.getProcessInstance() != this) {
            subject.setProcessInstance(this);
        }
    }

    public boolean hasActiveSubjects() {
        return getActiveSubjects().count() > 0;
    }

    public Optional<Subject> findActiveSubject(SubjectModel subjectModel) {
        return filterToOne(getActiveSubjects(), subject -> subject.getSubjectModel().equalsId(subjectModel));
    }

    private Stream<Subject> getActiveSubjects() {
        return getSubjects().stream()
                .filter(Subject::isActive);
    }

    public Collection<ObjectInstance> getObjectInstances() {
        return emptyOrUnmodifiableSet(objectInstances);
    }

    public Optional<ObjectInstance> getObjectInstance(ObjectModel objectModel) {
        return getObjectInstances().stream()
                .filter(objectInstance -> objectInstance.getObjectModel().equalsId(objectModel))
                .reduce(toOne());
    }

    public ObjectInstance addObjectInstance(ObjectModel objectModel) {
        ObjectInstance objectInstance = new ObjectInstance(objectModel, this);
        objectInstances = lazyAdd(objectInstances, objectInstance);
        return objectInstance;
    }

    /**
     * Return the read-only values for the given ObjectModel or return an empty
     * map.
     *
     * @param objectModel
     * @return
     */
    public Map<Long, Serializable> getValues(ObjectModel objectModel) {
        Objects.requireNonNull(objectModel, "objectModel must not be null");
        return getObjectInstance(objectModel)
                .map(objectInstance -> objectInstance.getValue())
                .orElse(Collections.emptyMap());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("processModel", processModel)
                .toString();
    }

}
