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
import java.util.Map;
import org.junit.Test;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.spi.TaskExecutionException;
import org.opensbpm.engine.api.spi.TaskExecutionProvider;
import org.opensbpm.engine.api.spi.TaskExecutionProvider.ResourceService;
import org.opensbpm.engine.core.engine.TaskProviderManager;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class TaskProviderManagerIT extends ServiceITCase {

    @Autowired
    private TaskProviderManager taskProviderManager;

    @Test
    public void testGetProviders() {
        //given

        //when
        Collection<TaskExecutionProvider> providers = taskProviderManager.getProviders();

        //then
        assertThat(providers, is(not(empty())));
    }

    @Test
    public void testFindProvider_FirstProvider() {
        //given
        String providerName = "FirstProvider";

        //when
        TaskExecutionProvider executionProvider = taskProviderManager.findProvider(providerName);

        //then
        assertThat(executionProvider, instanceOf(FirstProvider.class));
    }

    @Test
    public void testFindProvider_SecondProvider() {
        //given
        String providerName = "SecondProvider";

        //when
        TaskExecutionProvider executionProvider = taskProviderManager.findProvider(providerName);

        //then
        assertThat(executionProvider, instanceOf(SecondProvider.class));
    }

    @Component
    public static class FirstProvider implements TaskExecutionProvider {

        @Override
        public String getName() {
            return "FirstProvider";
        }

        @Override
        public ResourceService getResourceService() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public NextState executeTask(Map<String, String> parameters, Task task) throws TaskExecutionException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    @Component
    public static class SecondProvider implements TaskExecutionProvider {

        @Override
        public String getName() {
            return "SecondProvider";
        }

        @Override
        public ResourceService getResourceService() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public NextState executeTask(Map<String, String> parameters, Task task) throws TaskExecutionException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
