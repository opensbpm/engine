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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import org.opensbpm.engine.core.engine.entities.User;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;

@Entity
public class UserSubjectModel extends SubjectModel {

    //TODO add and test orphan remove of role
    @ManyToMany(
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
//    @JoinTable(name = "subjectmodeluserrole",
//            joinColumns = @JoinColumn(name = "subjectmodeluser"),
//            inverseJoinColumns = @JoinColumn(name = "role"),
//            uniqueConstraints = {
//                @UniqueConstraint(columnNames = {"subjectmodeluser", "role"})
//            }
//    )
    private Set<Role> roles;

    protected UserSubjectModel() {
    }

    public UserSubjectModel(String name, List<Role> roles) {
        super(name);
        this.roles = new HashSet<>(roles);
    }

    public Collection<Role> getRoles() {
        return emptyOrUnmodifiableSet(roles);
    }

    /**
     * get all users from all roles
     *
     * @return
     */
    public Stream<User> getAllUsers() {
        return getRoles().stream()
                .flatMap(role -> role.getAllUsers().stream());
    }

    @Override
    public <T> T accept(SubjectModelVisitor<T> visitor) {
        return visitor.visitUserSubjectModel(this);
    }

}
