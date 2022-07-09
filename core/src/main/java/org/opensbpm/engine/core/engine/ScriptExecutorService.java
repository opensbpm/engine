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

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.ObjectBean;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.State;
import org.springframework.stereotype.Component;

@Component
public class ScriptExecutorService {

    private final ScriptEngine scriptEngine;

    public ScriptExecutorService(ScriptEngine scriptEngine) {
        this.scriptEngine = Objects.requireNonNull(scriptEngine, "ScriptEngine must be non null");
    }

    public String evaluteStateDisplayName(Subject subject, State state) {
        return Optional.ofNullable(state.getDisplayName())
                .map(displayName -> evalStateScript(String.format("\"%s\"", displayName), subject.getProcessInstance(), state))
                .orElse(state.getName());
    }

    private String evalStateScript(String script, ProcessInstance processInstance, State state) {
        return eval(script, bindings -> {
            processInstance.getProcessModel().getObjectModels().stream().forEach(objectModel -> {
                ObjectBean objectBean = createObjectBean(processInstance, state, objectModel);
                bindings.put(objectModel.getName(), objectBean);
            });
        });
    }

    private ObjectBean createObjectBean(ProcessInstance processInstance, State state, ObjectModel objectModel) {
        ObjectSchema objectSchema = new ObjectSchemaConverter(state)
                .convertToObjectSchema(objectModel);

        return ObjectBean.from(objectSchema, processInstance.getValues(objectModel));
    }

    public String evaluteObjectDisplayName(ObjectModel objectModel, FunctionState state, AttributeStore store) {
        return objectModel.getDisplayName()
                .map(displayName -> evalDisplayNameScript(String.format("\"%s\"", displayName), objectModel, state, store))
                .orElse(objectModel.getName());
    }

    private String evalDisplayNameScript(String script, ObjectModel objectModel, FunctionState state, AttributeStore attributeStore) {
        return eval(script, bindings -> {
            ObjectSchema objectSchema = new ObjectSchemaConverter(state)
                    .convertToObjectSchema(objectModel);

            Map<Long, Serializable> data = attributeStore.toIdMap(attributeModel -> state.hasAnyPermission(attributeModel));
            ObjectBean objectBean = ObjectBean.from(objectSchema, data);

            for (AttributeSchema attributeSchema : objectBean.getAttributeModels()) {
                bindings.put(attributeSchema.getName(), objectBean.get(attributeSchema.getName()));
            }
        });
    }

    private String eval(String script, Consumer<Bindings> bindingsConsumer) {
        try {
            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindingsConsumer.accept(bindings);

            //eval returns GString; convert it with toString()
            return scriptEngine.eval(script, bindings).toString();
        } catch (ScriptException ex) {
            Logger.getLogger(EngineConverter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return ex.getMessage();
        }
    }

}
