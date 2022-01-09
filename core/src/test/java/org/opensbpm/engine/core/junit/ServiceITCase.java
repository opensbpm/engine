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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaDelete;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensbpm.engine.api.UserTokenService.TokenRequest;
import org.opensbpm.engine.api.events.EngineEvent;
import org.opensbpm.engine.api.model.definition.ProcessDefinition;
import org.opensbpm.engine.core.junit.ServiceITConfig.EngineEventsCollector;
import org.opensbpm.engine.core.model.ProcessDefinitionPersistor;
import org.opensbpm.engine.core.model.ProcessModelService;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.opensbpm.engine.utils.StreamUtils.oneOrMoreAsList;

/**
 * Abstract Spring-Boot Test-Case for Service-Layer related Integration-Tests.
 *
 * {@link ServiceITConfig}
 *
 */
@ContextConfiguration(classes = ServiceITConfig.class)
@SpringBootTest
@RunWith(SpringRunner.class)
public abstract class ServiceITCase {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    protected TransactionTemplate transactionTemplate;

    @Autowired
    protected EngineEventsCollector engineEventsCollector;

    @Autowired
    private ProcessDefinitionPersistor definitionPersistor;

    @Autowired
    private ProcessModelService processModelService;

    @Before
    public void setUp() throws Exception {
        transactionTemplate = new TransactionTemplate(platformTransactionManager);
        engineEventsCollector.clear();
    }

    @After
    public void tearDown() {
        doInTransaction(() -> {
            //truncate complete database
            entityManager.flush();
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            entityManager.getMetamodel().getEntities().forEach(entityType -> {
                CriteriaDelete criteriaDelete = entityManager.getCriteriaBuilder().createCriteriaDelete(entityType.getJavaType());
                criteriaDelete.from(entityType);
                entityManager.createQuery(criteriaDelete).executeUpdate();
            });
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();

            return null;
        });
    }

    protected <T> T doInTransaction(Callable<T> action) throws TransactionException {
        Objects.requireNonNull(transactionTemplate, "transactionTemplate is null; setUp was not called");
        return doInTransaction(transactionTemplate, action);
    }

    public static <T> T doInTransaction(TransactionTemplate transactionTemplate, Callable<T> action) throws TransactionException {
        return transactionTemplate.execute(status -> {
            try {
                return action.call();
            } catch (Exception ex) {
                Logger.getLogger(ServiceITCase.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    protected ProcessModel saveProcessDefinition(ProcessDefinition processDefinition) {
        ProcessModel processModel = definitionPersistor.saveDefinition(processDefinition);
        return processModelService.save(processModel);
    }

    protected void assertEngineEvents(Matcher<EngineEvent<?>> eventMatcher) {
        assertEngineEvents(oneOrMoreAsList(eventMatcher));
    }

    protected void assertEngineEvents(Matcher<? super EngineEvent<?>> eventMatcher, Matcher<? super EngineEvent<?>>... eventMatchers) {
        assertEngineEvents(oneOrMoreAsList(eventMatcher, eventMatchers));
    }

    private void assertEngineEvents(List<Matcher<? super EngineEvent<?>>> matchers) {
        assertThat(engineEventsCollector, containsInAnyOrder(matchers));

        engineEventsCollector.clear();
    }

    protected static TokenRequest createTokenRequest(String username, String... roles) {
        return new TokenRequest() {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public Set<String> getRoles() {
                return new HashSet<>(Arrays.asList(roles));
            }
        };
    }
}
