/*
 * Purpose: Verifies Sprint 1 report generation APIs.
 * Why it exists: The MVP requires durable report generation plus PDF and CSV download from decision outputs.
 * Architecture fit: End-to-end backend coverage for the Reports bounded context without external reporting services.
 */
package com.airural.platform.core.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for generated report APIs. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_reports_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class ReportPlatformIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void reportGenerationCreatesPdfAndCsvExports() throws Exception {
        String token = registerAdmin();
        JsonNode analysis = json(postJson("/api/v1/decision/analyze", token, """
                {
                  "problemStatement":"Village water access is unreliable and evidence shows frequent bore well downtime.",
                  "surveyEvidence":{"domain":"water","householdsAffected":38},
                  "mlPredictions":{"risk":"HIGH","confidence":0.82},
                  "agentOutputs":{"knowledgeCitations":["policy-water-001"]},
                  "requireHumanApproval":true
                }
                """, 200));
        String decisionId = analysis.at("/data/id").asText();

        JsonNode report = json(postJson("/api/v1/reports", token, """
                {
                  "decisionId":"%s",
                  "organizationId":"00000000-0000-0000-0000-000000000001",
                  "reportType":"EXECUTIVE",
                  "title":"Executive Village Water Report"
                }
                """.formatted(decisionId), 201));
        String reportId = report.at("/data/id").asText();
        assertThat(report.at("/data/executiveSummary").asText()).contains("root cause");

        byte[] pdf = mockMvc.perform(get("/api/v1/reports/" + reportId + "/pdf").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(new String(pdf)).startsWith("%PDF");

        String csv = mockMvc.perform(get("/api/v1/reports/" + reportId + "/csv").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(csv).contains("root_cause").contains("recommendation");
    }

    private String registerAdmin() throws Exception {
        return json(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"report.admin",
                                  "email":"report.admin@example.gov",
                                  "fullName":"Report Admin",
                                  "password":"VeryStrongPassword123!",
                                  "organizationCode":"PLATFORM"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).at("/data/accessToken").asText();
    }

    private String postJson(String path, String token, String payload, int expectedStatus) throws Exception {
        return mockMvc.perform(post(path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}
