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
import java.util.HashMap;
import java.util.Map;
import org.opensbpm.engine.api.instance.AttributeSchema;
import org.opensbpm.engine.api.instance.AttributeStore;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectBean;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;

class ObjectDataCreator {

    public static boolean hasAnyPermission(ObjectModel objectModel, FunctionState state, AttributeSchema attributeSchema) {
        return objectModel.getAllAttributeModels()
                .filter(attribute -> attribute.getId().equals(attributeSchema.getId()))
                .findFirst()
                .map(attribute -> state.hasAnyPermission(attribute))
                .orElse(false);
    }

    
    private final ScriptExecutorService scriptExecutorService;

    public ObjectDataCreator(ScriptExecutorService scriptExecutorService) {
        this.scriptExecutorService = scriptExecutorService;
    }

    public ObjectData createObjectData(ObjectInstance objectInstance, FunctionState state) {
        ObjectModel objectModel = objectInstance.getObjectModel();

        ObjectSchema objectSchema = ObjectSchemaConverter.toObjectSchema(state, objectModel);
        AttributeStore attributeStore = new AttributeStore(objectSchema, new HashMap<>(objectInstance.getValue()));
        ObjectBean objectBean = ObjectBean.from(objectSchema, objectInstance.getValue());

        return createObjectData(objectModel, state, objectBean, attributeStore);
    }

    public ObjectData createObjectData(ObjectModel objectModel, FunctionState state,ObjectBean objectBean, AttributeStore attributeStore) {
        Map<Long, Serializable> data = attributeStore.toIdMap(attributeSchema -> hasAnyPermission(objectModel, state, attributeSchema));

        return ObjectData.of(objectModel.getName())
                .withData(data)
                .withId(attributeStore.getId())
                .withDisplayName(evalObjectDisplayName(objectModel, objectBean))
                .build();
    }

    private String evalObjectDisplayName(ObjectModel objectModel, ObjectBean objectBean) {
        return scriptExecutorService.evaluteObjectDisplayName(objectModel, objectBean);
    }

}
