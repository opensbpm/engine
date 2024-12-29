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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.MessageModel;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.StateVisitor;
import org.opensbpm.engine.core.model.entities.StateVisitor.OptionalStateAdapter;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;
import static org.opensbpm.engine.utils.StreamUtils.filterToOne;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity
/* 
    there is no unique-key; on recursions there are multiple subjects (with
    different user of the same role) per subjectmodel 
 */
public abstract class Subject implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //TODO remove CascadeType.ALL
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "processinstance", nullable = false, updatable = false)
    private ProcessInstance processInstance;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subjectmodel", nullable = false, updatable = false)
    protected SubjectModel subjectModel;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "subject")
    @MapKey(name = "lastModified")
    //@OrderBy("lastModified,id")
    private final SortedMap<Long, SubjectTrail> subjectTrail = new TreeMap<>();
    //
    private transient SubjectTrail currentTrail;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "subject")
    private List<Message> messages;

    protected Subject() {
    }

    protected Subject(ProcessInstance processInstance, SubjectModel subjectModel) {
        setProcessInstance(processInstance);
        this.subjectModel = subjectModel;
    }

    @Override
    public Long getId() {
        return id;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
        if (processInstance != null && !processInstance.getSubjects().contains(this)) {
            processInstance.addSubject(this);
        }
    }

    public abstract SubjectModel getSubjectModel();

    public Optional<FunctionState> getVisibleCurrentState() {
        //PENDING find usages and remove duplicatins
        return getVisibleState(getCurrentState());
    }

    public Optional<FunctionState> getVisibleState(State state) {
        return Optional.of(state)
                .map(t -> t.accept(new OptionalStateAdapter<FunctionState>() {

            @Override
            public Optional<FunctionState> visitFunctionState(FunctionState functionState) {
                return Optional.of(functionState);
            }

            @Override
            public Optional<FunctionState> visitReceiveState(ReceiveState receiveState) {
                //recurse over messageModels.head till functionState (or break with Optional.empty()
                return filterToOne(receiveState.getMessageModels(), message
                        -> hasUnconsumedMessages(message.getObjectModel()))
                        .map(MessageModel::getHead)
                        .map(headState -> getVisibleState(headState))
                        .orElse(Optional.empty());
            }

        }))
                .orElse(Optional.empty());
    }

    public <T> T getCurrent(StateVisitor<T> visitor) {
        return getCurrentState().accept(visitor);
    }

    public State getCurrentState() {
        return getCurrentTrail().map(trail -> trail.getState()).orElse(null);
    }

    public LocalDateTime getLastChanged() {
        return getCurrentTrail().map(trail -> LocalDateTime.ofInstant(new Date(trail.getLastModified()).toInstant(), ZoneId.systemDefault())).orElse(null);
    }

    private Optional<SubjectTrail> getCurrentTrail() {
        if (currentTrail == null && !subjectTrail.isEmpty()) {
            //cache for performance reason
            currentTrail = subjectTrail.get(subjectTrail.lastKey());
        }
        return Optional.ofNullable(currentTrail);
    }

    public void setCurrentState(State currentState) {
        currentTrail = new SubjectTrail(this, currentState);
        subjectTrail.put(currentTrail.getLastModified(), currentTrail);
    }

    public boolean isActive() {
        return !subjectTrail.isEmpty() && !getCurrentState().isEnd();
    }

    public Message addMessage(ObjectModel objectModel, Subject sender) {
        Message message = new Message(objectModel, sender);
        messages = lazyAdd(messages, message);
        return message;
    }

    private Collection<Message> getUnconsumedMessages() {
        return emptyOrUnmodifiableList(messages).stream()
                .filter(message -> !message.isConsumed())
                .collect(Collectors.toList());
    }

    /**
     * get all unconsumed messages
     *
     * @return
     */
    public Collection<Message> getUnconsumedMessages(ObjectModel objectModel) {
        return getUnconsumedMessages().stream()
                .filter(message -> message.getObjectModel().equalsId(objectModel))
                .collect(Collectors.toList());
    }

    public boolean hasUnconsumedMessages(ObjectModel objectModel) {
        Objects.requireNonNull(objectModel);
        return getUnconsumedMessages(objectModel).size() > 0;
    }

    public abstract <T> T accept(SubjectVisitor<T> visitor);

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("processInstance", (processInstance == null ? StringUtils.EMPTY : processInstance.getId()))
                .append("subjectModel", (subjectModel == null ? StringUtils.EMPTY : subjectModel.getId()))
                .toString();
    }

}
