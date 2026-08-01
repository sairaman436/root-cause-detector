/*
 * Purpose: Verifies secured decision intelligence API workflows.
 * Why it exists: Milestone 10 requires integration tests for analysis, root-cause, recommendation, explanation, confidence, and history.
 * Architecture fit: End-to-end backend coverage without predictive ML training or external integrations.
 */
package com.airural.platform.core.decision;

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

/** Integration tests for decision intelligence APIs. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_decision_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class DecisionPlatformIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void decisionFlowWorksThroughSecuredApis() throws Exception {
        String token = registerAdmin();

        JsonNode analysis = json(postJson("/api/v1/decision/analyze", token, """
                {
                  "problemStatement":"Fluoride contamination is recurring in village bore wells and households report health symptoms.",
                  "surveyEvidence":{"waterQuality":"fluoride","householdsAffected":42,"policy":"rural drinking water scheme"},
                  "mlPredictions":{"risk":"HIGH","confidence":0.81},
                  "agentOutputs":{"rootCauseAgreement":0.8},
                  "requireHumanApproval":true
                }
                """));
        String decisionId = analysis.at("/data/id").asText();
        assertThat(decisionId).isNotBlank();
        assertThat(analysis.at("/data/rootCauses").size()).isGreaterThanOrEqualTo(1);
        assertThat(analysis.at("/data/recommendations").size()).isGreaterThanOrEqualTo(1);
        assertThat(analysis.at("/data/humanApprovalRequired").asBoolean()).isTrue();

        JsonNode rootCause = json(postJson("/api/v1/decision/root-cause", token, """
                {
                  "problemStatement":"School attendance is dropping after water access disruption.",
                  "evidenceContext":{"surveyEvidence":"attendance drop","policy":"education support"}
                }
                """));
        assertThat(rootCause.at("/data/rootCauses").size()).isGreaterThanOrEqualTo(1);

        JsonNode recommendation = json(postJson("/api/v1/decision/recommend", token, """
                {
                  "objective":"Recommend a compliant intervention for household water access",
                  "context":{"surveyEvidence":"water shortage","policy":"drinking water scheme"}
                }
                """));
        assertThat(recommendation.at("/data/recommendations").size()).isGreaterThanOrEqualTo(1);

        JsonNode loaded = json(getJson("/api/v1/decision/" + decisionId, token));
        assertThat(loaded.at("/data/id").asText()).isEqualTo(decisionId);

        JsonNode explanation = json(getJson("/api/v1/decision/explanation/" + decisionId, token));
        assertThat(explanation.at("/data/reasoningTrace").size()).isGreaterThanOrEqualTo(6);
        assertThat(explanation.at("/data/hypotheses").size()).isGreaterThanOrEqualTo(3);

        JsonNode confidence = json(getJson("/api/v1/decision/confidence/" + decisionId, token));
        assertThat(confidence.at("/data/overallConfidence").asDouble()).isGreaterThan(0.0);
        assertThat(confidence.at("/data/reasonCodes").size()).isGreaterThanOrEqualTo(1);

        JsonNode history = json(getJson("/api/v1/decision/history", token));
        assertThat(history.at("/data/content").size()).isGreaterThanOrEqualTo(3);
    }

    private String registerAdmin() throws Exception {
        return json(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"decision.admin",
                                  "email":"decision.admin@example.gov",
                                  "fullName":"Decision Admin",
                                  "password":"VeryStrongPassword123!",
                                  "organizationCode":"PLATFORM"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).at("/data/accessToken").asText();
    }

    private String getJson(String path, String token) throws Exception {
        return mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String postJson(String path, String token, String payload) throws Exception {
        return mockMvc.perform(post(path).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}
