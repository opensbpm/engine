package org.opensbpm.engine.samplee2e;

import org.springframework.context.ConfigurableApplicationContext;

public interface WorkflowOrchestrator {

    void execute(ConfigurableApplicationContext context);
}
