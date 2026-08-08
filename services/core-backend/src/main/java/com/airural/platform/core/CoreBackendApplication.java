/*
 * Purpose: Starts the Spring Boot core backend service.
 * Why it exists: Provides the independently runnable backend for identity, survey, evidence, AI, decision, reporting, audit, and operations workflows.
 * Architecture fit: Anchors the approved modular Spring Boot backend boundary for enterprise workflows.
 */
package com.airural.platform.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBackendApplication.class, args);
    }
}
