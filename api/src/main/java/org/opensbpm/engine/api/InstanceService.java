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
import org.opensbpm.engine.api.instance.AuditTrail;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInstanceState;

public interface InstanceService {

    //TODO add find-method with SearchFilter
    Collection<ProcessInfo> findAllByStates(Set<ProcessInstanceState> states);

    ProcessInfo stopProcess(ProcessRequest processRequest) throws ProcessNotFoundException;

    Collection<AuditTrail> getAuditTrail(ProcessRequest processRequest) throws ProcessNotFoundException;

    public static interface ProcessRequest {

        /**
         * Create an new instance of a {@link ProcessRequest} with the given id.
         *
         * @param id must be a valid {@link ProcessRequest#id}
         * @return a new instance
         */
        public static ProcessRequest of(Long id) {
            return () -> id;
        }

        /**
         * Create an new instance of a {@link ProcessRequest} with the given {@link ProcessInfo}.
         *
         * @param processInfo must be a valid {@link ProcessInfo}
         * @return a new instance
         */
        public static ProcessRequest of(ProcessInfo processInfo) {
            return () -> processInfo.getId();
        }

        Long getId();
    }

}
