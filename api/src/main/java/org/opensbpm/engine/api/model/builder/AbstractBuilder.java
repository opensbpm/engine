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
package org.opensbpm.engine.api.model.builder;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractBuilder<V,T extends AbstractBuilder<V,T>> implements Builder<V> {

    private static final Logger LOGGER = Logger.getLogger(AbstractBuilder.class.getName());

    private V value;

    protected abstract T self();
    
    @Override
    public final V build() {
        if (value == null) {
            value = create();
            LOGGER.log(Level.FINE, "created {0}", value);
        }
        return value;
    }

    protected abstract V create();

    protected final void checkBuilt() {
        if (value != null) {
            throw new IllegalStateException(getClass().getName() + " is already built");
        }
    }

}
