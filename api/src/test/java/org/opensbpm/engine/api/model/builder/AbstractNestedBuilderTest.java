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
package org.opensbpm.engine.api.model.builder;

import java.util.List;
import org.junit.Test;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AbstractNestedBuilder;
import org.opensbpm.engine.api.model.builder.ObjectBuilder.AttributeBuilder;
import org.opensbpm.engine.api.model.definition.ObjectDefinition;
import static org.junit.Assert.fail;

public class AbstractNestedBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetAttributeWithWrongname() {
        AttributeBuilder<?> result = new AbstractNestedBuilder("Test") {
            @Override
            protected ObjectDefinition.NestedAttribute create(String name, List attributes) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }.getAttribute("Test");
        fail("getAttribute with wrong name must throw IllegalArgumentException but was " + result);
    }

}
