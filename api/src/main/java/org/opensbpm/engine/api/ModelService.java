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
 *****************************************************************************
 */
package org.opensbpm.engine.api;

import java.util.Collection;
import java.util.Set;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.api.model.ProcessModelState;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;

/**
 * Service interface to operate with all kind of process model operations.
 *
 */
public interface ModelService {

    Collection<ProcessModelInfo> findAllByStates(Set<ProcessModelState> states);

    ProcessDefinition retrieveDefinition(ModelRequest modelRequest) throws ModelNotFoundException;

    ProcessModelInfo save(ProcessDefinition processDefinition);

    void updateState(ModelRequest modelRequest, ProcessModelState newState) throws ModelNotFoundException;

    void delete(ModelRequest modelRequest) throws ModelNotFoundException;

    /**
     * Wraps a {@link ProcessModelInfo#getId()} in a request object.
     */
    public static interface ModelRequest {

        /**
         * Create an new instance of a {@link ModelRequest} with the given id.
         *
         * @param id must match the id from a {@link ProcessModelInfo}
         * @return a new instance
         */
        public static ModelRequest of(Long id) {
            return () -> id;
        }

        /**
         * Create an new instance of a {@link ModelRequest} with the given {@link ProcessModelInfo}.
         *
         * @param modelInfo must be a valid {@link ProcessModelInfo}
         * @return a new instance
         */
        public static ModelRequest of(ProcessModelInfo modelInfo) {
            return () -> modelInfo.getId();
        }

        Long getId();
    }

}
