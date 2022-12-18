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
 * ****************************************************************************
 */
package org.opensbpm.engine.core.engine;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.instance.AuditTrail;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.ProcessInfo;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo;
import org.opensbpm.engine.api.instance.ProcessInfo.SubjectStateInfo.StateFunctionType;
import org.opensbpm.engine.api.instance.RoleToken;
import org.opensbpm.engine.api.instance.TaskInfo;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.engine.entities.SubjectTrail;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.model.ModelConverter;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ReceiveState;
import org.opensbpm.engine.core.model.entities.SendState;
import org.opensbpm.engine.core.model.entities.State;
import org.opensbpm.engine.core.model.entities.StateVisitor;
import org.springframework.stereotype.Component;
import static org.opensbpm.engine.core.engine.entities.SubjectVisitor.userSubject;

@Component
public class EngineConverter {

    private final ScriptExecutorService scriptExecutorService;

    public EngineConverter(ScriptExecutorService scriptExecutorService) {
        this.scriptExecutorService = scriptExecutorService;
    }

    public List<ProcessInfo> convertInstances(Collection<ProcessInstance> processInstances) {
        return processInstances.stream()
                .map(process -> convertInstance(process))
                .collect(Collectors.toList());
    }

    public ProcessInfo convertInstance(ProcessInstance processInstance) {
        return new ProcessInfo(
                processInstance.getId(),
                ModelConverter.convertModel(processInstance.getProcessModel()),
                convertUser(processInstance.getStartUser()),
                processInstance.getState(),
                processInstance.getStartTime(),
                processInstance.getEndTime(),
                convertSubjects(processInstance)
        );
    }

    private List<SubjectStateInfo> convertSubjects(ProcessInstance processInstance) {
        //PENDING don't filter UserSubjects
        return processInstance.getSubjects().stream()
                .filter(subject -> subject instanceof UserSubject)
                .map(UserSubject.class::cast)
                .map(subject -> convertSubject(subject))
                .collect(Collectors.toList());
    }

    private SubjectStateInfo convertSubject(UserSubject subject) {
        return new SubjectStateInfo(subject.getId(),
                convertUser(subject),
                subject.getSubjectModel().getName(),
                evaluteStateDisplayName(subject, subject.getCurrentState()),
                convertFunctionType(subject),
                subject.getLastChanged());
    }

    private static UserToken convertUser(Subject subject) {
        return subject.accept(userSubject())
                .map(userSubject
                        -> userSubject.getUser() == null
                ? null
                : convertUser(userSubject.getUser()))
                .orElse(null);
    }

    private static StateFunctionType convertFunctionType(UserSubject subject) {
        return subject.getCurrent(new StateVisitor<StateFunctionType>() {
            @Override
            public StateFunctionType visitFunctionState(FunctionState functionState) {
                return StateFunctionType.FUNCTION;
            }

            @Override
            public StateFunctionType visitReceiveState(ReceiveState receiveState) {
                return StateFunctionType.RECEIVE;
            }

            @Override
            public StateFunctionType visitSendState(SendState sendState) {
                return StateFunctionType.SEND;
            }
        });
    }

    public TaskInfo convertSubjectState(Subject subject, State state) {
        return new TaskInfo(subject.getId(),
                subject.getProcessInstance().getId(),
                subject.getProcessInstance().getProcessModel().getName(),
                evaluteStateDisplayName(subject, state),
                subject.getLastChanged()
        );
    }

    public List<AuditTrail> convertAuditTrails(List<SubjectTrail> subjectTrails) {
        return subjectTrails.stream()
                .sorted((SubjectTrail o1, SubjectTrail o2) -> {
                    int result = Long.valueOf(o1.getLastModified()).compareTo(o2.getLastModified());
                    if (0 == result) {
                        result = o1.getId().compareTo(o2.getId());
                    }
                    return result;
                })
                .map(this::convertTrail)
                .collect(Collectors.toList());
    }

    private AuditTrail convertTrail(SubjectTrail subjectTrail) {
        return new AuditTrail(
                subjectTrail.getLastModifiedDateTime(),
                subjectTrail.getSubject().getSubjectModel().getName(),
                convertUser(subjectTrail.getSubject()),
                evaluteStateDisplayName(subjectTrail.getSubject(), subjectTrail.getState())
        );
    }

    public TaskResponse createTaskResponse(Subject subject) {
        FunctionState state = subject.getVisibleCurrentState()
                .orElseThrow(() -> new IllegalStateException("visible state " + subject.getVisibleCurrentState().toString() + " not FunctionState"));

        List<NextState> nextStates = state.getHeads().stream()
                .map(nextState -> getNextState(subject, nextState))
                .collect(Collectors.toList());

        return new TaskResponseConverter(scriptExecutorService).convert(subject, state, nextStates);
    }

    private NextState getNextState(Subject subject, State nextState) {
        return subject.getVisibleState(nextState)
                .filter(functionState -> !functionState.isEnd())
                .map(functionState -> NextState.of(functionState.getId(), evaluteStateDisplayName(subject, functionState)))
                .orElse(NextState.ofEnd(nextState.getId(), evaluteStateDisplayName(subject, nextState)));
    }

    public String evaluteStateDisplayName(Subject subject, State state) {
        return scriptExecutorService.evaluteStateDisplayName(subject, state);
    }

    public static UserToken convertUser(User user) {
        return UserToken.of(user.getId(), user.getName(), user.getRoles().stream()
                .map(role -> RoleToken.of(role.getId(), role.getName()))
                .collect(Collectors.toSet())
        );
    }

}
