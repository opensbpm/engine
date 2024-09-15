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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import static org.opensbpm.engine.utils.StreamUtils.emptyOrUnmodifiableList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AutocompleteResponse implements Serializable {

    public static AutocompleteResponse of(Collection<Autocomplete> autocompletes) {
        AutocompleteResponse autocompleteResponse = new AutocompleteResponse();
        autocompleteResponse.autocompletes = new ArrayList<>(autocompletes);
        return autocompleteResponse;
    }

    protected AutocompleteResponse() {
        //JAXB constructor
    }

    public List<Autocomplete> getAutocompletes() {
        return emptyOrUnmodifiableList(autocompletes);
    }

    private List<Autocomplete> autocompletes;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Autocomplete {

        public static Autocomplete of(ObjectData objectData) {
            Autocomplete autocomplete = new Autocomplete();
            autocomplete.objectData = objectData;
            return autocomplete;
        }

        private ObjectData objectData;

        protected Autocomplete() {
            //JAXB constructor
        }

        public ObjectData getObjectData() {
            return objectData;
        }

    }
}
