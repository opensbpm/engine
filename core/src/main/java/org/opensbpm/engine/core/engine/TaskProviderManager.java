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
package org.opensbpm.engine.core.engine;

import org.opensbpm.engine.api.spi.TaskExecutionProvider;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskProviderManager {

    @Autowired
    private List<TaskExecutionProvider> providers;

    public List<TaskExecutionProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    public TaskExecutionProvider findProvider(String providerName) {
        return getProviders().stream()
                .filter(provider -> provider.getName().equals(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No TaskExecutionProvider for '" + providerName + "' found"));
    }

}
