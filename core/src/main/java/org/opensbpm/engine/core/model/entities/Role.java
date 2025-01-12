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
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.utils.entities.HasId;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableSet;
import static org.opensbpm.engine.utils.StreamUtils.lazyAdd;

@Entity(name = "roles")
public class Role implements HasId, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(name = "roles_user")
    private Set<User> users;

    protected Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<User> getUsers() {
        return emptyOrUnmodifiableSet(users);
    }

    public void addUser(User user) {
        Objects.requireNonNull(user, "user must not be null");
        users = lazyAdd(users, user);
        if (!user.getRoles().contains(this)) {
            user.addRole(this);
        }
    }

    public void removeUser(User user) {
        if (getUsers().contains(user)) {
            users.remove(user);
        }
        if (user.getRoles().contains(this)) {
            user.removeRole(this);
        }
    }

    /**
     * get all users of this role, exactly get the users of groups and direct users
     *
     * @return
     */
    public Set<User> getAllUsers() {
        return getUsers();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("roleId", getId())
                .append("name", getName())
                .toString();
    }
}
