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
package org.opensbpm.engine.core.engine.entities;

import org.opensbpm.engine.core.engine.entities.UserSubject;
import org.opensbpm.engine.core.engine.entities.Subject;
import org.opensbpm.engine.api.model.definition.StateDefinition.StateEventType;
import org.opensbpm.engine.core.junit.EntityTestCase;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.State;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SubjectTest extends EntityTestCase<Subject> {

    public SubjectTest() {
        super(Subject.class);
    }

    @Test
    public void testIsActive() {
        //given
        Subject instance = new UserSubject();
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));

        //when
        State state = new FunctionState("function");
        instance.setCurrentState(state);

        //then
        assertTrue(instance.isActive());
    }

    @Test
    public void testIsActive_withEnd() {
        //given
        Subject instance = new UserSubject();
        ProcessModel processModel = new ProcessModel("name", new ModelVersion(0, 0));

        //when
        State state = new FunctionState("function");
        state.setEventType(StateEventType.END);
        instance.setCurrentState(state);

        //then
        assertFalse(instance.isActive());
    }

}
