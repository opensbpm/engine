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
package org.opensbpm.engine.core.engine;

import java.util.Arrays;
import javax.script.ScriptEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensbpm.engine.api.instance.AutocompleteResponse;
import org.opensbpm.engine.api.spi.AutocompleteProvider;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.engine.taskprovider.DummyAutocompleteProvider;
import org.opensbpm.engine.core.model.entities.FunctionState;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.opensbpm.engine.core.model.entities.Role;
import org.opensbpm.engine.core.model.entities.UserSubjectModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opensbpm.engine.core.junit.MockData.spyFunctionState;
import static org.opensbpm.engine.core.junit.MockData.spyObjectModel;
import static org.opensbpm.engine.core.junit.MockData.spyProcessInstance;
import static org.opensbpm.engine.core.junit.MockData.spyProcessModel;
import static org.opensbpm.engine.core.junit.MockData.spyUser;

@SpringBootTest(classes = {
    ValidationService.class,
    ValidationProviderManager.class,
    AutocompleteProvider.class,
    DummyAutocompleteProvider.class
})
@RunWith(SpringRunner.class)
public class ValidationServiceTest {

    @MockBean
    private ScriptExecutorService scriptExceutorService;

    @MockBean
    private ScriptEngine scriptEngine;

    @Autowired
    private ValidationService validationService;

    @Test
    public void testCreateAutocompleteResponse() {
        //given

        User user = spyUser(1L, "User Name", "First Name", "Last Name");

        ProcessModel processModel = spyProcessModel(2l, "Process Name");
        ProcessInstance processInstance = spyProcessInstance(3l, processModel, user);
        UserSubjectModel userSubjectModel = processModel.addUserSubjectModel("subject", Arrays.asList(new Role("role")));
        FunctionState state = spyFunctionState(1l, userSubjectModel, "Function");

        ObjectModel objectModel = spyObjectModel(1l, processModel, "Object Model");

        String queryString = null;

        //when
        AutocompleteResponse response = validationService.createAutocompleteResponse(state, objectModel, queryString);

        //then        
        assertThat(response, is(notNullValue()));
        assertThat(response.getAutocompletes(), is(notNullValue()));
        //assertThat(response.getAutocompletes(), is(not(empty())));
    }

}
