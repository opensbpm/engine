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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.model.entities.SubjectModel;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import static org.opensbpm.engine.core.model.entities.SubjectModelVisitor.userSubjectModel;

@Entity(name = "subjectuser")
public class UserSubject extends Subject {

    @ManyToOne
    @JoinColumn(name = "uId")
    private User user;

    protected UserSubject() {
    }

    public UserSubject(ProcessInstance processInstance, SubjectModel subjectModel, User user) {
        super(processInstance, subjectModel);
        this.user = user;
    }

    @Override
    public UserSubjectModel getSubjectModel() {
        return subjectModel.accept(userSubjectModel())
                .orElseThrow(() -> new IllegalStateException(subjectModel + " not type of UserSubjectModel"));
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Objects.requireNonNull(user);
    }

    @Override
    public <T> T accept(SubjectVisitor<T> visitor) {
        //Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitUserSubject(this);
    }

    /**
     * get the assigned user or if this subject is unassigned get all users of subject-model;also see {@link UserSubjectModel#getAllUsers()
     * }
     *
     * @return
     */
    public Stream<User> getCurrentOrAllUsers() {
        return Optional.ofNullable(getUser())
                .map(Stream::of)
                .orElse(getSubjectModel().getAllUsers());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("user", user)
                .append("processInstance", (getProcessInstance() == null ? StringUtils.EMPTY : getProcessInstance().getId()))
                .append("subjectModel", (subjectModel == null ? StringUtils.EMPTY : getSubjectModel().getName()))
                .toString();
    }

}
