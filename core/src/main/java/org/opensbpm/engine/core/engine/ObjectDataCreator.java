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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectData.ObjectDataBuilder;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.ObjectBean;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;

class ObjectDataCreator {

    private final ScriptEngine scriptEngine;

    public ObjectDataCreator(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public ObjectData createObjectData(ObjectInstance objectInstance, FunctionState state) {
        ObjectModel objectModel = objectInstance.getObjectModel();
        AttributeStore attributeStore = objectInstance.getAttributeStore();

        return createObjectData(objectModel, state, attributeStore);
    }

    public ObjectData createObjectData(ObjectModel objectModel, FunctionState state, AttributeStore attributeStore) {
        ObjectSchema objectSchema = new ObjectSchemaConverter(state)
                .convertToObjectSchema(objectModel);
        
        Map<Long, Serializable> data = attributeStore.toIdMap(attributeModel -> state.hasAnyPermission(attributeModel));
        ObjectBean objectBean = ObjectBean.from(objectSchema, data);
        
        ObjectDataBuilder objectDataBuilder = ObjectData.of(objectModel.getName())
                .withData(data)
                .withId(attributeStore.getId());
        
        objectModel.getDisplayName()
                .map(displayName -> evalDisplayNameScript(String.format("\"%s\"", displayName), objectBean))
                .ifPresent(evaluatetDisplayName -> objectDataBuilder.withDisplayName(evaluatetDisplayName));
        
        return objectDataBuilder.build();
    }

    private String evalDisplayNameScript(String script, ObjectBean objectBean) throws RuntimeException {
        try {
            Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            for (AttributeSchema attributeSchema : objectBean.getAttributeModels()) {
                bindings.put(attributeSchema.getName(), objectBean.get(attributeSchema.getName()));
            }
            //eval returns GString; convert it with toString()
            return scriptEngine.eval(script, bindings).toString();
        } catch (ScriptException ex) {
            Logger.getLogger(EngineConverter.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return ex.getMessage();
        }
    }

}
