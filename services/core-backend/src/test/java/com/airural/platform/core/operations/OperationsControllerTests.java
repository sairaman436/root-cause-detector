/*
 * Purpose: Verifies production operations APIs expose version and deployment metadata.
 * Why it exists: Protects release automation and health dashboards from accidental endpoint regressions.
 * Architecture fit: Supports Milestone 11 operational API quality gates.
 */
package com.airural.platform.core.operations;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

/** Tests for operations metadata endpoints. */
class OperationsControllerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OperationsController controller = new OperationsController(null, null, "test", "test-region");
        mockMvc = standaloneSetup(controller).build();
    }

    @Test
    void versionReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/platform/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.service", is("core-backend")))
                .andExpect(jsonPath("$.data.environment", is("test")));
    }

    @Test
    void deploymentStatusReturnsOperationalEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/platform/deployment-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SERVING")))
                .andExpect(jsonPath("$.data.metricsEndpoint", is("/actuator/prometheus")));
    }
}
