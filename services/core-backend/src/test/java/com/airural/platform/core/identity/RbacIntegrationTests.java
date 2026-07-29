/*
 * Purpose: Verifies RBAC enforcement for identity administration APIs.
 * Why it exists: Milestone 2 requires role- and permission-based authorization.
 * Architecture fit: Integration coverage for admin identity management boundaries.
 */
package com.airural.platform.core.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for RBAC-protected administration APIs. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_identity_rbac_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class RbacIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void administratorCanManageIdentityCatalogs() throws Exception {
        String accessToken = registerAdmin("rbac.admin@example.gov", "rbac.admin");

        mockMvc.perform(get("/api/v1/admin/permissions").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        String orgPayload = """
                {
                  "name": "District Operations",
                  "code": "DISTRICT-OPS"
                }
                """;
        JsonNode organization = objectMapper.readTree(mockMvc.perform(post("/api/v1/admin/organizations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orgPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        assertThat(organization.at("/data/code").asText()).isEqualTo("DISTRICT-OPS");
    }

    private String registerAdmin(String email, String username) throws Exception {
        String registerPayload = """
                {
                  "username": "%s",
                  "email": "%s",
                  "fullName": "RBAC Admin",
                  "password": "VeryStrongPassword123!",
                  "organizationCode": "PLATFORM"
                }
                """
                .formatted(username, email);

        return objectMapper
                .readTree(mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerPayload))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .at("/data/accessToken")
                .asText();
    }
}
