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
package org.opensbpm.engine.api.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opensbpm.engine.api.adapters.SerializableAdapter;
import org.opensbpm.engine.utils.StreamUtils;

@XmlAccessorType(value = XmlAccessType.FIELD)
public class Options {

    public static Options of(List<Serializable> values) {
        Options options = new Options();
        options.values = new ArrayList<>(values);
        return options;
    }
    
    @XmlJavaTypeAdapter(SerializableAdapter.class)
    private List<Serializable> values;

    protected Options() {
    }

    public List<Serializable> getValues() {
        return StreamUtils.emptyOrUnmodifiableList(values);
    }

}
