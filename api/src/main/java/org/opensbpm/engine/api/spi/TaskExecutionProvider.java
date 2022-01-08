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
package org.opensbpm.engine.api.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.taskprovider.TaskProviderInfo.ProviderResource;

public interface TaskExecutionProvider {

    String getName();

    default ResourceService getResourceService() {
        return new ResourceService() {
            @Override
            public Collection<ProviderResource> getResources() {
                return Collections.emptyList();
            }

            @Override
            public void addResource(ProviderResource providerResource) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    NextState executeTask(Map<String, String> parameters, Task task) throws TaskExecutionException;

    interface ResourceService {

        Collection<ProviderResource> getResources();

        void addResource(ProviderResource providerResource);
    }
}
