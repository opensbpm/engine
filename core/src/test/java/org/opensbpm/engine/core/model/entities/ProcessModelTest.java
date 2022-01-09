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
package org.opensbpm.engine.core.model.entities;

import java.util.Collection;
import org.junit.Test;
import org.opensbpm.engine.core.junit.EntityTestCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.fail;

public class ProcessModelTest extends EntityTestCase<ProcessModel> {

    public ProcessModelTest() {
        super(ProcessModel.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSubjectModelsUnmodifiable() {
        //given
        ProcessModel instance = new ProcessModel();

        //when
        Collection<SubjectModel> result = instance.getSubjectModels();
        result.add(new ServiceSubjectModel("Test"));
        fail("Collection from getSubjectModels must not be modifiable");
    }

    @Test
    public void testAddSubjectModel() throws Exception {
        testAddOneToMany("subjectModel", SubjectModel.class, UserSubjectModel.class);
    }

    @Test
    public void testAddObjectModel() throws Exception {
        //given
        ProcessModel processModel = new ProcessModel();

        //when
        ObjectModel objectModel = processModel.addObjectModel("Object");

        //then
        assertThat("ProcessModel.getObjectModels",
                processModel.getObjectModels(), contains(objectModel));
    }

}
