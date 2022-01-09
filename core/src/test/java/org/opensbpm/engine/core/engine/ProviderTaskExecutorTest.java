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

import java.time.LocalDateTime;
import java.util.Map;
import javax.script.ScriptEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.spi.TaskExecutionException;
import org.opensbpm.engine.api.spi.TaskExecutionProvider;
import org.opensbpm.engine.core.engine.ProviderTaskChangedObserver.ProviderTaskExecutor;
import org.opensbpm.engine.core.engine.ProviderTaskChangedObserver.ServiceSubjectRepository;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.ServiceSubject;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.ServiceSubjectModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opensbpm.engine.core.junit.MockData.spyProcessInstance;
import static org.opensbpm.engine.core.junit.MockData.spyProcessModel;
import static org.opensbpm.engine.core.junit.MockData.spyServiceSubject;
import static org.opensbpm.engine.core.junit.MockData.spyUser;

/**
 * Spring Mock Unit-Test for {@link ProviderTaskExecutor}
 */
@SpringBootTest(classes = {
    ProviderTaskExecutor.class,
    EngineConverter.class
})
@RunWith(SpringRunner.class)
public class ProviderTaskExecutorTest {

    @Autowired
    private ProviderTaskExecutor providerTaskExecutor;

    @MockBean
    private StateChangeService stateChangeService;

    @MockBean
    private TaskProviderManager taskProviderManager;

    @MockBean
    private ValidationProviderManager validationProviderManager;

    @MockBean
    private ServiceSubjectRepository subjectRepository;

    @MockBean
    private ProcessInstanceService processInstanceService;

    @MockBean
    private ScriptEngine scriptEngine;

    @Test
    public void testExecuteTaskWithTaskExecutionException() {
        //given
        final String providerName = "Test Provider";

        TaskInfo taskInfo = new TaskInfo(1l, 1l, "processName", "stateName", LocalDateTime.MIN);

        Long pmId = 1l;
        Long userId = 2l;
        Long piId = 3l;
        Long sId = 4l;

        User user = spyUser(userId, "username", "firstname", "lastname");
        //when(userService.findById(userId)).thenReturn(Optional.of(user));

        ProcessModel processModel = spyProcessModel(pmId, "ProcessModel Name");

        ProcessInstance processInstance = spyProcessInstance(piId, processModel, user);
        ServiceSubjectModel subjectModel = processModel.addServiceSubjectModel("subject");

        ServiceSubject subject = spyServiceSubject(sId, processInstance, subjectModel);
        final FunctionState currentState = new FunctionState("Provider Task");
        currentState.setProviderName(providerName);
        currentState.addHead(new FunctionState("End"));
        subject.setCurrentState(currentState);

        when(subjectRepository.getOne(anyLong())).thenReturn(subject);

        when(taskProviderManager.findProvider(providerName)).thenReturn(new TaskExecutionProvider() {
            @Override
            public String getName() {
                return providerName;
            }

            @Override
            public NextState executeTask(Map<String, String> parameters, Task task) throws TaskExecutionException {
                throw new TaskExecutionException("something badly happend");
            }
        });

        //when
        providerTaskExecutor.executeTask(taskInfo);

        //then
        verify(processInstanceService, times(1)).cancelBySystem(any(ProcessInstance.class), anyString());
    }

}
