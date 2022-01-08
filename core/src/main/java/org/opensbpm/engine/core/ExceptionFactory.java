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

import org.opensbpm.engine.api.ModelNotFoundException;
import org.opensbpm.engine.api.ProcessNotFoundException;
import org.opensbpm.engine.api.UserNotFoundException;
import org.opensbpm.engine.api.instance.TaskNotFoundException;
import org.opensbpm.engine.api.instance.TaskOutOfDateException;
import org.opensbpm.engine.core.engine.entities.Subject;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.opensbpm.engine.core.engine.entities.SubjectVisitor.userSubject;

public final class ExceptionFactory {

    public static Supplier<ModelNotFoundException> newModelNotFoundException(Long modelId) {
        return () -> {
            String msg = MessageFormat.format("ProcessModel with id {0} not found", modelId);
            return new ModelNotFoundException(msg);
        };
    }

    public static Supplier<UserNotFoundException> newUserNotFoundException(String userName) {
        return () -> {
            String msg = MessageFormat.format("User {0} not found", userName);
            return new UserNotFoundException(msg);
        };
    }

    public static Supplier<TaskNotFoundException> newTaskNotFoundException(long taskId) {
        return () -> {
            String msg = MessageFormat.format("Task with id {0} not found", taskId);
            return new TaskNotFoundException(msg);
        };
    }

    public static Supplier<ProcessNotFoundException> newProcessNotFoundException(Long processId) {
        return () -> {
            String msg = MessageFormat.format("ProcessInstance with id {0} not found", processId);
            return new ProcessNotFoundException(msg);
        };
    }

    public static TaskOutOfDateException newTaskOutOfDateException(Subject subject, LocalDateTime lastChanged) {
        String userMessage = subject.accept(userSubject())
                .map(userSubject -> userSubject.getUser() == null ? "" : " of User " + userSubject.getUser().getUsername())
                .orElse("");
        String message = "Subject " + subject.getSubjectModel().getName() + userMessage + " changed since " + lastChanged;
        return new TaskOutOfDateException(message);
    }

    private ExceptionFactory() {
    }

}
