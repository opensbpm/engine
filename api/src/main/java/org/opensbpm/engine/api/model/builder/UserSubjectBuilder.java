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
package org.opensbpm.engine.api.model.builder;

import org.opensbpm.engine.api.model.definition.StateDefinition;
import org.opensbpm.engine.api.model.definition.SubjectDefinition.UserSubjectDefinition;

import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class UserSubjectBuilder extends SubjectBuilder<UserSubjectBuilder, UserSubjectDefinition> {

    private final List<String> roles = new ArrayList<>();

    public UserSubjectBuilder(String name, List<String> roles) {
        super(name);
        this.roles.addAll(roles);
    }

    public UserSubjectBuilder addRole(String role) {
        roles.add(role);
        return this;
    }

    @Override
    protected UserSubjectDefinition createSubject(List<StateDefinition> states, boolean starter) {
        return new UserSubjectDefinition() {
            @Override
            public String getName() {
                return UserSubjectBuilder.this.getName();
            }

            @Override
            public List<String> getRoles() {
                return emptyOrUnmodifiableList(roles);
            }

            @Override
            public boolean isStarter() {
                return starter;
            }

            @Override
            public List<StateDefinition> getStates() {
                return states;
            }
            
            @Override
            public String toString() {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append("name", getName())
                        .append("roles", getRoles())
                        .append("starter", isStarter())
                        .append("states", getStates())
                        .toString();
            }

        };
    }

}
