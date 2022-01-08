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

import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectData.ObjectDataBuilder;
import org.opensbpm.engine.core.model.entities.AttributeModel;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

class ObjectDataCreator {

    private final ScriptEngine scriptEngine;

    public ObjectDataCreator(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public ObjectData createObjectData(ObjectModel objectModel, FunctionState state, AttributeStore attributeStore) {
        Map<Long, Serializable> data = attributeStore.toIdMap(attributeModel -> state.hasAnyPermission(attributeModel));
        ObjectBean objectBean = new ObjectBean(objectModel, attributeStore);
        
        ObjectDataBuilder objectDataBuilder = ObjectData.of(objectModel.getName())
                .withData(data)
                .withId(attributeStore.getId());
        
        objectModel.getDisplayName()
                .map(displayName -> evalObjectScript(String.format("\"%s\"", displayName), objectBean))
                .ifPresent(evaluatetDisplayName -> objectDataBuilder.withDisplayName(evaluatetDisplayName));
        
        return objectDataBuilder.build();
    }

    private String evalObjectScript(String script, ObjectBean objectBean) throws RuntimeException {
        try {
            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            for (AttributeModel attributeModel : objectBean.getAttributeModels()) {
                bindings.put(attributeModel.getName(), objectBean.get(attributeModel.getName()));
            }
            //eval returns GString; convert it with toString()
            return scriptEngine.eval(script, bindings).toString();
        } catch (ScriptException ex) {
            Logger.getLogger(EngineConverter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return ex.getMessage();
        }
    }

}
