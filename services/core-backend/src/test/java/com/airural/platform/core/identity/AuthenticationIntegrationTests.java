/*
 * Purpose: Verifies the identity authentication API end to end.
 * Why it exists: Registration, login, refresh, logout, and current-user lookup are Milestone 2 core workflows.
 * Architecture fit: Integration coverage for the approved identity platform.
 */
package com.airural.platform.core.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

/** Integration tests for authentication workflows. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_identity_auth_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class AuthenticationIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrationLoginRefreshLogoutAndCurrentUserWork() throws Exception {
        String registerPayload = """
                {
                  "username": "admin.user",
                  "email": "admin@example.gov",
                  "fullName": "Admin User",
                  "phoneNumber": "+10000000000",
                  "password": "VeryStrongPassword123!",
                  "organizationCode": "PLATFORM"
                }
                """;

        JsonNode register = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        String accessToken = register.at("/data/accessToken").asText();
        String refreshToken = register.at("/data/refreshToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        JsonNode me = objectMapper.readTree(mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        assertThat(me.at("/data/email").asText()).isEqualTo("admin@example.gov");
        assertThat(me.at("/data/roles").toString()).contains("ADMINISTRATOR");

        String loginPayload = """
                {
                  "email": "admin@example.gov",
                  "password": "VeryStrongPassword123!"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginPayload))
                .andExpect(status().isOk());

        String refreshPayload = "{\"refreshToken\":\"" + refreshToken + "\"}";
        JsonNode refreshed = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        assertThat(refreshed.at("/data/accessToken").asText()).isNotBlank();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshed.at("/data/refreshToken").asText() + "\"}"))
                .andExpect(status().isOk());
    }
}
