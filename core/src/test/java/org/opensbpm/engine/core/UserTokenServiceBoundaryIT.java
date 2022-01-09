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
package org.opensbpm.engine.core;

import org.junit.Test;
import org.opensbpm.engine.api.UserTokenService.TokenRequest;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

//@ContextConfiguration(classes = {
//    UserTokenServiceBoundary.class,
//    UserService.class,
//    UserRepositoryService.class,
//    RoleService.class,
//    RoleRepositoryService.class
//})
//@Import(SbpmJpaConfig.class)
//@RunWith(SpringRunner.class)
public class UserTokenServiceBoundaryIT extends ServiceITCase {

    @Autowired
    private UserTokenServiceBoundary userTokenServiceBoundary;

    @Test
    public void testRegisterUser() {
        //given
        TokenRequest tokenRequest = createTokenRequest("Username", "Role 1", "Role 2");

        //when
        UserToken userToken = doInTransaction(() -> userTokenServiceBoundary.registerUser(tokenRequest));

        //then
        assertThat(userToken.getName(), is("Username"));
    }

    @Test
    public void testRetrieveToken() {
        //given
        TokenRequest tokenRequest = createTokenRequest("Username", "Role 1", "Role 2");
        doInTransaction(() -> userTokenServiceBoundary.registerUser(tokenRequest));

        //when
        UserToken userToken = doInTransaction(() -> userTokenServiceBoundary.retrieveToken(tokenRequest));

        //then
        assertThat(userToken.getName(), is("Username"));
    }

}
