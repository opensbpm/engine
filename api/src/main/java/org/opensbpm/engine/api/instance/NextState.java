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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
public class NextState implements Serializable {

    public static NextState of(Long id, String name) {
        NextState nextState = new NextState();
        nextState.id = id;
        nextState.name = name;
        return nextState;
    }

    public static NextState ofEnd(Long id, String name) {
        NextState nextState = NextState.of(id, name);
        nextState.end = true;
        return nextState;
    }

    private Long id;

    @EqualsExclude
    private String name;

    @EqualsExclude
    private boolean end;

    public NextState() {
        //JAXB constructor
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * is this state the (temporary) end state for this cycle?
     *
     *
     * @return <code>true</code> if this state is last state of a chain of states
     */
    public boolean isEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("end", isEnd())
                .toString();
    }
}
