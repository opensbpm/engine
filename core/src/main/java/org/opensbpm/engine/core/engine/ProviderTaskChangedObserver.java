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
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.core.engine.entities.ServiceSubject;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.api.spi.TaskExecutionProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.opensbpm.engine.core.utils.repositories.JpaSpecificationRepository;

@Component
public class ProviderTaskChangedObserver {

    @Autowired
    private ProviderTaskExecutor providerTaskExecutor;

    @Async
    @EventListener
    public void handleTaskChangedEvent(ProviderTaskChangedEvent taskChangedEvent) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "received {0}", taskChangedEvent.getSource());
        if (Type.CREATE == taskChangedEvent.getType()) {
            providerTaskExecutor.executeTask(taskChangedEvent.getSource());
        }
    }

    @Component
    public static class ProviderTaskExecutor {
        //inner class for easier integeration-testing

        @Autowired
        private StateChangeService stateChangeService;

        @Autowired
        private TaskProviderManager taskProviderManager;

        @Autowired
        private ServiceSubjectRepository subjectRepository;

        @Autowired
        private ProcessInstanceService processInstanceService;

        @Autowired
        private EngineConverter engineConverter;
        @Transactional
        public void executeTask(TaskInfo taskInfo) {
            //PENDING subjectService.findWithLock(taskInfo.getId());
            ServiceSubject subject = subjectRepository.getOne(taskInfo.getId());
            FunctionState functionState = subject.getVisibleCurrentState()
                    .orElseThrow(() -> new IllegalStateException("no visible current state"));

            TaskExecutionProvider taskExecutionProvider = taskProviderManager.findProvider(functionState.getProviderName());

            //retrieve Task 
            Task task = new Task(taskInfo, engineConverter.createTaskResponse(subject));

            try {
                NextState nextState = taskExecutionProvider.executeTask(functionState.getParameters(), task);
                stateChangeService.changeState(subject, task.createTaskRequest(nextState));
            } catch (Exception ex) {
                //catch all exceptions from taskExecutionProvider here
                Logger.getLogger(ProviderTaskChangedObserver.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                processInstanceService.cancelBySystem(subject.getProcessInstance(), ex.getMessage());
            }
        }

    }

    @Repository
    public interface ServiceSubjectRepository extends JpaSpecificationRepository<ServiceSubject, Long> {

    }

}
