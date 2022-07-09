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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;
import org.opensbpm.engine.api.instance.AutocompleteResponse;
import org.opensbpm.engine.api.instance.AutocompleteResponse.Autocomplete;
import org.opensbpm.engine.api.instance.ObjectData;
import org.opensbpm.engine.api.instance.ObjectSchema;
import org.opensbpm.engine.api.instance.SourceMap;
import org.opensbpm.engine.api.spi.AutocompleteProvider;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private final ScriptEngine scriptEngine;
    private final ValidationProviderManager validationProviderManager;

    public ValidationService(ScriptEngine scriptEngine, ValidationProviderManager validationProviderManager) {
        this.scriptEngine = scriptEngine;
        this.validationProviderManager = validationProviderManager;
    }

    public AutocompleteResponse createAutocompleteResponse(FunctionState state, ObjectModel objectModel, String queryString) {
        ObjectSchema objectSchema = new ObjectSchemaConverter(state).convertToObjectSchema(objectModel);
        List<Autocomplete> autocompletes = new ArrayList<>();
        for (AutocompleteProvider autocompleteProvider : validationProviderManager.getAutocompleteProvider()) {
            autocompleteProvider.getAutocomplete(objectSchema, queryString).stream()
                    .map(sourceMap -> toObjectData(objectModel, state, sourceMap))
                    .map(objectData -> Autocomplete.of(objectData))
                    .collect(Collectors.toCollection(() -> autocompletes));
        }
        return AutocompleteResponse.of(autocompletes);
    }

    private ObjectData toObjectData(ObjectModel objectModel, FunctionState state, SourceMap sourceMap) {
        return new ObjectDataCreator(scriptEngine)
                .createObjectData(objectModel, state, new AttributeStore(objectModel, sourceMap));
    }

}
