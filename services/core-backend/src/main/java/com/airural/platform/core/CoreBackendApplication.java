/*
 * Purpose: Starts the Spring Boot core backend service shell.
 * Why it exists: Provides an independently runnable backend foundation with actuator health endpoints before business modules are implemented.
 * Architecture fit: Anchors the approved modular Spring Boot backend boundary for future enterprise workflows.
 */
package com.airural.platform.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBackendApplication.class, args);
    }
}
