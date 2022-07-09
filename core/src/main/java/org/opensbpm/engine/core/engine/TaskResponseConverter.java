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
import java.util.Objects;
import javax.script.ScriptEngine;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.TaskResponse;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.model.entities.FunctionState;
import static org.opensbpm.engine.utils.StreamUtils.mapToList;

class TaskResponseConverter {

    private final ScriptEngine scriptEngine;

    public TaskResponseConverter(ScriptEngine scriptEngine) {
        this.scriptEngine = Objects.requireNonNull(scriptEngine, "ScriptEngine must be non null");
    }

    public TaskResponse convert(Subject subject, FunctionState state, List<NextState> nextStates) {
        ProcessInstance processInstance = subject.getProcessInstance();

        List<ObjectSchema> objectSchemas = new ObjectSchemaConverter(state)
                .createObjectSchemas(processInstance.getProcessModel());

        List<ObjectData> datas = mapToList(processInstance.getObjectInstances(),
                objectInstance -> new ObjectDataCreator(scriptEngine)
                        .createObjectData(objectInstance, state));

        return TaskResponse.of(subject.getId(), nextStates, subject.getLastChanged(), objectSchemas, datas);
    }


}
