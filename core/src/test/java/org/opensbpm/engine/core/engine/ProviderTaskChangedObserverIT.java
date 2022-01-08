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

import org.opensbpm.engine.api.events.EngineEvent.Type;
import org.opensbpm.engine.api.events.ProviderTaskChangedEvent;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.core.engine.ProviderTaskChangedObserver.ProviderTaskExecutor;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.junit.Test;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

public class ProviderTaskChangedObserverIT extends ServiceITCase {

    @Autowired
    private ApplicationEventPublisher publisher;

    @MockBean
    private ProviderTaskExecutor providerTaskExecutor;

    @Test
    public void testHandleServiceTask() {
        //given
        TaskInfo taskInfo = new TaskInfo();

        //when
        publisher.publishEvent(new ProviderTaskChangedEvent(taskInfo, Type.CREATE));

        //then
        verify(providerTaskExecutor, timeout(500l).atLeastOnce()).executeTask(taskInfo);
    }

}
