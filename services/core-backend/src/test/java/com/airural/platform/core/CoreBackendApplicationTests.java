/*
 * Purpose: Verifies the Spring Boot backend foundation can start.
 * Why it exists: Protects the operational service shell before business modules are added.
 * Architecture fit: Supports the backend build gate in CI/CD and local development.
 */
package com.airural.platform.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class CoreBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
