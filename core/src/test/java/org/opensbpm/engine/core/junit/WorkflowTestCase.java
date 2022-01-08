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
package org.opensbpm.engine.core.junit;

import org.opensbpm.engine.api.EngineService;
import org.opensbpm.engine.api.ModelService;
import org.opensbpm.engine.api.instance.UserToken;
import org.opensbpm.engine.core.UserTokenServiceBoundary;
import org.opensbpm.engine.core.engine.UserSubjectService;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.engine.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.opensbpm.engine.api.InstanceService;

/**
 * Abstract Spring-Boot Test-Case for Workflow related Integration-Tests.
 *
 * It loads everything needed to execute complete {@link ProcessInstance}'s
 *
 * {@link ServiceITConfig}
 *
 */
public abstract class WorkflowTestCase extends ServiceITCase {

    @Autowired
    protected UserTokenServiceBoundary authenticationService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ModelService modelService;

    @Autowired
    protected ProcessModelService processModelService;

    @Autowired
    protected InstanceService instanceService;

    @Autowired
    protected EngineService engineService;

    @Autowired
    protected UserSubjectService userSubjectService;

    public UserProcessController createUserController(String userName, String role) {
        UserToken userToken = doInTransaction(()
                -> authenticationService.registerUser(createTokenRequest(userName, role)));
        return new UserProcessController(userService, instanceService, engineService, transactionTemplate, userToken);
    }

}
