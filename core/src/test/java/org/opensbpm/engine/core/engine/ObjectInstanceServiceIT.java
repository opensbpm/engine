package org.opensbpm.engine.core.engine;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Test;
import org.opensbpm.engine.core.engine.entities.ObjectInstance;
import org.opensbpm.engine.core.engine.entities.ProcessInstance;
import org.opensbpm.engine.core.engine.entities.User;
import org.opensbpm.engine.core.junit.ServiceITCase;
import org.opensbpm.engine.core.model.entities.ModelVersion;
import org.opensbpm.engine.core.model.entities.ObjectModel;
import org.opensbpm.engine.core.model.entities.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.TransactionTemplate;

public class ObjectInstanceServiceIT extends ServiceITCase {

    @Autowired
    private ObjectInstanceService service;

    @Test
    public void testAddManyObjectModels() throws Exception {
        //given
        TestEntityManager testEntityManager = new TestEntityManager(entityManagerFactory);
        ProcessModel processModel = doInTransaction(() -> {
            ProcessModel model = new ProcessModel("name", new ModelVersion(0, 0));
            model.addObjectModel("name");
            return testEntityManager.persistAndFlush(model);
        });
        ObjectModel objectModel = processModel.getObjectModels().stream()
                .filter(model -> model.getName().equals("name"))
                .findFirst()
                .orElseThrow();
        ProcessInstance processInstance = doInTransaction(() -> {
            User user = testEntityManager.persist(new User("userName"));
            return testEntityManager.persistAndFlush(new ProcessInstance(processModel, user));
        });

        //when
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.range(0, 10).boxed()
                .map(idx -> new Runnable() {
            @Override
            public void run() {
                Long id = doInTransaction(() -> {
                    ObjectInstance objectInstance = service.retrieveObjectInstance(processInstance, objectModel);
                    objectInstance.setValue(new HashMap<>());
                    return testEntityManager.persistAndGetId(objectInstance, Long.class);
                });
                assertThat(id, is(notNullValue()));
            }
        })
                .forEach(callable -> executorService.submit(callable));
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertThat(processInstance.getObjectInstances(), hasSize(1));
    }

}
