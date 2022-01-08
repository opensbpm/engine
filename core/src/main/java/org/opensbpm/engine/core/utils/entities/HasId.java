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
package org.opensbpm.engine.core.utils.entities;

import java.util.Objects;

/**
 * Interface for JPA-Entities to handle general retrievement of Entites-ID
 */
public interface HasId {

    /**
     * retrieve the primary identifier of a JPA-Entity. {@code getId()} returns null if the entity is not peristent.
     *
     * @return for peristent/existing entites the id other <code>null</code>
     */
    Long getId();

    /**
     * equals check basend on {@link #getId()}. Two entites are considered equals if and only if
     * <ul>
     * <li><strike>both entites have the same class {@code this.getClass() == other.getClass()}</strike></li>
     * <li>both entites have an id {@code this.getId() != null && other.getId() != null}</li>
     * <li>both entites have the same id {@code this.getId().equals(other.getId())}</li>
     * </ul>
     *
     * @param other {@link HasId} implemententation of the same type
     * @return <code>true</code> if both entites are equal
     * @throws NullPointerException if other is <code>null</code>
     */
    default boolean equalsId(HasId other) {
        Objects.requireNonNull(other, "other must be non null");
        //type check doesn't work with Hibernate Proxies
//        return getClass() == other.getClass()
        return getId() != null
                && getId().equals(other.getId());
    }

}
