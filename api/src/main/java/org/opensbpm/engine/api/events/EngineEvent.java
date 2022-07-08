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
package org.opensbpm.engine.api.events;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * a generic EngineEvent.
 *
 * @param <T> The type of the source-object on which the EngineEvent initially occurred.
 */
public abstract class EngineEvent<T extends Serializable> implements Serializable {

    public enum Type {
        CREATE,
        UPDATE,
        DELETE
    }
    private final T source;
    private final Type type;

    protected EngineEvent(T source, Type type) {
        this.source = source;
        this.type = type;
    }

    public T getSource() {
        return source;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", type)
                .append("source", getSource())
                .toString();

    }
}
