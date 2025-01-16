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
package org.opensbpm.engine.core.junit;

import java.util.Collection;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;
import org.opensbpm.engine.api.EngineService;
import org.opensbpm.engine.api.InstanceService;
import org.opensbpm.engine.api.ModelNotFoundException;
import org.opensbpm.engine.api.ModelService.ModelRequest;
import org.opensbpm.engine.api.ProcessNotFoundException;
import org.opensbpm.engine.api.UserNotFoundException;
import org.opensbpm.engine.api.instance.AuditTrail;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo;
import org.opensbpm.engine.api.instance.ProcessInstanceState;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskNotFoundException;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.api.model.ProcessModelInfo;
import org.opensbpm.engine.core.engine.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opensbpm.engine.api.junit.ProcessInfoMatchers.isState;
import static org.opensbpm.engine.core.ExceptionFactory.newProcessNotFoundException;
import static org.opensbpm.engine.core.junit.ServiceITCase.doInTransaction;

public class UserProcessController {

    private final UserService userService;
    private final InstanceService instanceService;
    private final EngineService engineService;
    private final TransactionTemplate transactionTemplate;
    private final UserToken userToken;

    public UserProcessController(UserService userService, InstanceService processService, EngineService engineService, TransactionTemplate transactionTemplate, UserToken userToken) {
        this.userService = userService;
        this.instanceService = processService;
        this.engineService = engineService;
        this.transactionTemplate = transactionTemplate;
        this.userToken = userToken;
    }

    public String getUsername() {
        return userToken.getName();
    }

    public Long getId() {
        return userService.findByName(getUsername()).get().getId();
    }

    public ProcessInstanceController startProcess(ProcessModelInfo modelInfo) throws UserNotFoundException, ModelNotFoundException {
        TaskInfo taskInfo = engineService.startProcess(userToken, ModelRequest.of(modelInfo));
        return new ProcessInstanceController(taskInfo.getProcessId());
    }

    public Collection<TaskInfo> getTasks() throws UserNotFoundException {
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        return engineService.getTasks(userToken);
    }

    public TestTask getTask(String taskName) throws UserNotFoundException, TaskNotFoundException, TaskOutOfDateException {
        return getTask(findTaskInfo(taskName));
    }

    private TaskInfo findTaskInfo(String stateName) throws UserNotFoundException {
        return getTasks().stream()
                .filter((task) -> task.getStateName().equals(stateName))
                .findFirst()
                .orElseThrow(() -> {
                    try {
                        return new IllegalStateException("Task '" + stateName + "' not found, possible tasknames are " + getTaskNamesAsString());
                    } catch (UserNotFoundException ex) {
                        Logger.getLogger(UserProcessController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        return new IllegalStateException(ex.getMessage(), ex);
                    }
                });
    }

    private String getTaskNamesAsString() throws UserNotFoundException {
        return getTasks().stream()
                .map(taskInfo -> taskInfo.getStateName())
                .collect(Collectors.joining(","));
    }

    private TestTask getTask(TaskInfo taskInfo) throws TaskNotFoundException, TaskOutOfDateException {
        return new TestTask(taskInfo, engineService.getTaskResponse(userToken, taskInfo));
    }

    public boolean execute(TestTask task, String stateName) throws TaskOutOfDateException {
        return doInTransaction(transactionTemplate, () -> {
            return engineService.executeTask(userToken, task.createRequest(stateName));
        });
    }

    public void assertTasks(Matcher<? super Collection<TaskInfo>> matchers) throws UserNotFoundException {
        assertThat(String.format("wrong tasks for '%s'", getUsername()), getTasks(), matchers);
    }

    public class ProcessInstanceController {

        private final Long processId;

        private ProcessInstanceController(Long processId) {
            this.processId = processId;
        }

        public ProcessInfo getProcessInfo() throws ProcessNotFoundException {
            return instanceService.findAllByStates(EnumSet.allOf(ProcessInstanceState.class)).stream()
                    .filter(processInfo -> processId.equals(processInfo.getId()))
                    .findFirst()
                    .orElseThrow(newProcessNotFoundException(processId));
        }

        public Collection<AuditTrail> getAuditTrail() throws ProcessNotFoundException {
            return instanceService.getAuditTrail(getProcessInfo());
        }

        public void assertState(ProcessInstanceState processInstanceState, Matcher<Iterable<SubjectStateInfo>> subjectsMatcher) throws ProcessNotFoundException {
            ProcessInfo processInfo = getProcessInfo();
            assertThat("wrong ProcessState", processInfo, isState(processInstanceState));
            assertThat("wrong SubjectStates", processInfo.getSubjects(), subjectsMatcher);
        }

        public void assertTrail(Matcher<? super Collection<AuditTrail>> matcher) throws ProcessNotFoundException {
            assertThat("wrong trail", getAuditTrail(), matcher);
        }

    }
}
