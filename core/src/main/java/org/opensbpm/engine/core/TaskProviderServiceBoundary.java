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
package org.opensbpm.engine.core;

import java.util.Collection;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.TaskProviderService;
import org.opensbpm.engine.api.taskprovider.TaskProviderInfo;
import org.opensbpm.engine.api.taskprovider.TaskProviderInfo.ProviderResource;
import org.opensbpm.engine.core.engine.TaskProviderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskProviderServiceBoundary implements TaskProviderService {

    @Autowired
    private TaskProviderManager taskProviderManager;

    @Override
    public Collection<TaskProviderInfo> getProviders() {
        return taskProviderManager.getProviders().stream()
                .map(provider -> new TaskProviderInfo(provider.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ProviderResource> getResources(TaskProviderInfo taskProviderInfo) {
        return taskProviderManager.findProvider(taskProviderInfo.getName())
                .getResourceService()
                .getResources();
    }

    @Override
    public void addResource(TaskProviderInfo taskProviderInfo, ProviderResource providerResource) {
        taskProviderManager.findProvider(taskProviderInfo.getName())
                .getResourceService()
                .addResource(providerResource);
    }

}
