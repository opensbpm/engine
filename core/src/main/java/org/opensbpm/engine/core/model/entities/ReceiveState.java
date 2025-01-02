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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity
@Table(name = "statereceive")
public class ReceiveState extends State {

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "statereceive", nullable = false, updatable = false)
    private Set<MessageModel> messageModels;

    protected ReceiveState() {
    }

    public ReceiveState(String name) {
        super(name);
    }

    public Collection<MessageModel> getMessageModels() {
        return emptyOrUnmodifiableSet(messageModels);
    }

    /**
     * create a new {@link MessageModel} with the given objectModel and state and add it to this ReceiveState
     *
     * @param objectModel see {@link MessageModel#objectModel}
     * @param state see {@link MessageModel#head}
     * @return
     */
    public MessageModel addMessageModel(ObjectModel objectModel, State state) {
        MessageModel messageModel = new MessageModel(objectModel, state);
        messageModels = lazyAdd(messageModels, messageModel);
        return messageModel;
    }

    @Override
    public <T> T accept(StateVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitReceiveState(this);
    }

    @Override
    public Collection<State> getHeads() {
        return getMessageModels().stream()
                .map(MessageModel::getHead)
                .collect(Collectors.toList());
    }

}
