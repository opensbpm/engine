package org.opensbpm.engine.rest.services;

import org.opensbpm.engine.core.EngineConfig;
import org.opensbpm.engine.service.cxf.CxfConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableAutoConfiguration
@ComponentScan
@Import({CxfConfig.class, EngineConfig.class})
@Configuration
public class ServicesConfig {

}
