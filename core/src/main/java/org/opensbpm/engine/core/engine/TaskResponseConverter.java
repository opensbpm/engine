/** *****************************************************************************
 * Copyright (C) 2022 Stefan Sedelmaier
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
 *****************************************************************************
 */
package org.opensbpm.engine.core.engine;

import java.util.List;
import java.util.stream.Collectors;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.core.engine.ScriptExecutorService.BindingContext;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.model.entities.FunctionState;
import static org.opensbpm.engine.core.engine.entities.SubjectVisitor.userSubject;

class TaskResponseConverter {

    private final ScriptExecutorService scriptService;

    public TaskResponseConverter(ScriptExecutorService scriptService) {
        this.scriptService = scriptService;
    }

    public TaskResponse convert(Subject subject, FunctionState state, List<NextState> nextStates) {
        ProcessInstance processInstance = subject.getProcessInstance();
        BindingContext bindingContext = BindingContext.ofSubject(subject);

        List<ObjectSchema> objectSchemas = new ObjectSchemaConverter(scriptService, state, bindingContext)
                .createObjectSchemas(processInstance.getProcessModel());

        List<ObjectData> datas = processInstance.getObjectInstances().stream()
                .map(objectInstance -> new ObjectDataCreator(scriptService).createObjectData(objectInstance, state, bindingContext))
                .collect(Collectors.toList());

        return TaskResponse.of(subject.getId(), nextStates, subject.getLastChanged(), objectSchemas, datas);
    }

}
