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
 *****************************************************************************
 */
package org.opensbpm.engine.api.model.definition;

import java.util.List;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.functionState;
import static org.opensbpm.engine.api.model.builder.DefinitionFactory.userSubject;

public class SubjectStateGraphTest {

    @Test
    public void testGetRoots() {
        //given
        SubjectDefinition subjectDefinition = userSubject("user", "role")
                .addState(functionState("root")
                        .toHead(functionState("leaf"))
                )
                .build();
        SubjectStateGraph instance = new SubjectStateGraph(subjectDefinition);

        //when
        List<StateDefinition> roots = instance.getRoots();

        //then
        assertThat(roots, hasSize(1));
    }

    @Test
    public void testGetLeafs() {
        //given
        SubjectDefinition subjectDefinition = userSubject("user", "role")
                .addState(functionState("root")
                        .toHead(functionState("leaf"))
                )
                .build();
        SubjectStateGraph instance = new SubjectStateGraph(subjectDefinition);

        //when
        List<StateDefinition> leafs = instance.getLeafs();

        //then
        assertThat(leafs, hasSize(1));
    }

}
