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
package org.opensbpm.engine.core.engine.taskprovider;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.opensbpm.engine.api.instance.NextState;
import org.opensbpm.engine.api.instance.Task;
import org.opensbpm.engine.api.spi.TaskExecutionException;
import org.opensbpm.engine.api.spi.TaskExecutionProvider;
import org.opensbpm.engine.api.taskprovider.TaskProviderInfo.ProviderResource;
import org.springframework.stereotype.Component;

@Component
public class GroovyTaskProvider implements TaskExecutionProvider {

    public static final String NAME = "Groovy";
    public static final String SCRIPT = "script";
    private final ScriptEngine scriptEngine;

    public GroovyTaskProvider(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ResourceService getResourceService() {
        return new ResourceService() {
            @Override
            public Collection<ProviderResource> getResources() {
                return Collections.emptyList();
            }

            @Override
            public void addResource(ProviderResource providerResource) {
                //noop
            }
        };
    }

    @Override
    public NextState executeTask(Map<String, String> parameters, Task task) throws TaskExecutionException {
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("task", task);
        try {
            String script = parameters.get(SCRIPT);
            Object eval = scriptEngine.eval(script, bindings);
            if (eval == null) {
                if (task.getNextStates().size() == 1) {
                    return task.getNextStates().iterator().next();
                } else {
                    throw new TaskExecutionException("Script retourned 'null', cannot automaticly choose NextState from " + task.getNextStates().size() + " possible NextStates");
                }
            } else if (!(eval instanceof NextState)) {
                throw new IllegalStateException("Script returned not an instance of NextState:" + eval);
            }
            return (NextState) eval;
        } catch (ScriptException ex) {
            throw new TaskExecutionException(ex.getMessage(), ex);
        }
    }

}
