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
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"processmodel", "name"})
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class SubjectModel implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Column
    private boolean starter;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "subjectmodel", nullable = false, updatable = false)
    private List<State> states;

    protected SubjectModel() {
        //JPA constructor
    }

    protected SubjectModel(String name) {
        this.name = name;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isStarter() {
        return starter;
    }

    public void setStarter(boolean starter) {
        this.starter = starter;
    }

    public Collection<State> getStates() {
        return emptyOrUnmodifiableList(states);
    }

    /**
     * create a new {@link FunctionState} with the given name and add it to this SubjectModel
     *
     * @param name name of {@link FunctionState}, see {@link FunctionState#name}
     * @return
     */
    public FunctionState addFunctionState(String name) {
        return addState(new FunctionState(name));
    }

    /**
     * create a new {@link ReceiveState} with the given name and add it to this SubjectModel
     *
     * @param name name of {@link ReceiveState}, see {@link ReceiveState#name}
     * @return
     */
    public ReceiveState addReceiveState(String name) {
        return addState(new ReceiveState(name));
    }

    /**
     * create a new {@link SendState} with the given name and add it to this SubjectModel
     *
     * @param name name of {@link SendState}, see {@link SendState#name}
     * @param receiver receiver of {@link SendState}, see {@link SendState#receiver}
     * @param objectModel objectmodel of {@link SendState}, see {@link SendState#objectModel}
     * @return
     */
    public SendState addSendState(String name, SubjectModel receiver, ObjectModel objectModel) {
        return addState(new SendState(name, receiver, objectModel));
    }

    private <T extends State> T addState(T state) {
        states = lazyAdd(states, state);
        return state;
    }

    public abstract <T> T accept(SubjectModelVisitor<T> visitor);

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", name)
                .toString();
    }

}
