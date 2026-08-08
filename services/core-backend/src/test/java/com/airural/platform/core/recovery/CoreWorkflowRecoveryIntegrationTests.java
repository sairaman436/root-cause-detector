/*
 * Purpose: Verifies the recovered MVP workflow across bounded contexts.
 * Why it exists: Foundation recovery requires proof that authentication, surveys, submissions,
 * evidence, RAG, decision intelligence, and reporting execute together through public APIs.
 * Architecture fit: Cross-module API smoke coverage without bypassing Spring Security or persistence.
 */
package com.airural.platform.core.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** API-level recovery test for the platform's core user workflow. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:airural_recovery_flow_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "airural.evidence.local-storage-path=./target/recovery-evidence-storage",
        "airural.evidence.max-file-size-bytes=1048576"
})
class CoreWorkflowRecoveryIntegrationTests {
    private static final UUID PLATFORM_ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Authenticated users can create, submit, enrich, analyze, report, and retrieve the MVP workflow. */
    @Test
    void coreApplicationWorkflowWorksThroughSecuredApis() throws Exception {
        String token = registerAdmin();

        JsonNode survey = json(postJson("/api/v1/surveys", token, """
                {
                  "organizationId": "%s",
                  "name": "Recovery Village Water Survey",
                  "description": "Recovery test survey",
                  "tags": ["recovery", "water"]
                }
                """.formatted(PLATFORM_ORGANIZATION_ID), 200));
        String surveyId = survey.at("/data/id").asText();

        JsonNode section = json(postJson("/api/v1/surveys/" + surveyId + "/sections", token, """
                {
                  "code": "water_access",
                  "title": "Water Access",
                  "orderIndex": 1,
                  "repeatable": false
                }
                """, 200));
        String sectionId = section.at("/data/id").asText();

        JsonNode question = json(postJson("/api/v1/surveys/" + surveyId + "/questions", token, """
                {
                  "sectionId": "%s",
                  "code": "water_source",
                  "prompt": "Primary water source",
                  "questionType": "single_select",
                  "orderIndex": 1,
                  "required": true,
                  "options": [
                    {"value": "well", "label": "Well", "orderIndex": 1},
                    {"value": "tap", "label": "Tap", "orderIndex": 2}
                  ],
                  "validationRules": [
                    {"ruleType": "REQUIRED", "message": "Water source is required", "orderIndex": 1}
                  ]
                }
                """.formatted(sectionId), 200));
        String questionId = question.at("/data/id").asText();

        for (String status : new String[] {"REVIEW", "APPROVED", "PUBLISHED"}) {
            survey = json(postJson("/api/v1/surveys/" + surveyId + "/workflow", token, """
                    {"status": "%s", "reason": "recovery flow"}
                    """.formatted(status), 200));
        }
        assertThat(survey.at("/data/status").asText()).isEqualTo("PUBLISHED");

        JsonNode submission = json(postJson("/api/v1/surveys/" + surveyId + "/submissions", token, """
                {
                  "answers": [
                    {"questionId": "%s", "value": "well"}
                  ]
                }
                """.formatted(questionId), 200));
        assertThat(submission.at("/data/status").asText()).isEqualTo("SUBMITTED");

        JsonNode evidence = json(uploadEvidence(token, surveyId, questionId));
        String evidenceId = evidence.at("/data/id").asText();
        assertThat(evidence.at("/data/evidenceType").asText()).isEqualTo("IMAGE");

        JsonNode retrievedSurvey = json(getJson("/api/v1/surveys/" + surveyId, token));
        assertThat(retrievedSurvey.at("/data/id").asText()).isEqualTo(surveyId);

        json(postJson("/api/v1/ai/embed", token, """
                {
                  "text":"Village evidence shows unreliable well access and delayed water repairs.",
                  "collectionName":"knowledge",
                  "sourceType":"RECOVERY_TEST",
                  "embeddingModel":"bge-small-local",
                  "metadata":{"surveyId":"%s"}
                }
                """.formatted(surveyId), 200));

        JsonNode rag = json(postJson("/api/v1/ai/rag/query", token, """
                {
                  "query":"What evidence explains village water access failures?",
                  "collectionName":"knowledge",
                  "modelId":"qwen2.5-local",
                  "topK":3
                }
                """, 200));
        assertThat(rag.at("/data/citations").size()).isGreaterThanOrEqualTo(1);

        JsonNode decision = json(postJson("/api/v1/decision/analyze", token, """
                {
                  "surveyId":"%s",
                  "organizationId":"%s",
                  "evidenceIds":["%s"],
                  "problemStatement":"Village households report unreliable water access and delayed repairs.",
                  "surveyEvidence":{"surveyName":"Recovery Village Water Survey","answer":"well","ragAnswer":"%s"},
                  "mlPredictions":{"risk":"HIGH","confidence":0.78},
                  "agentOutputs":{"knowledgeCitations":%s},
                  "requireHumanApproval":true
                }
                """.formatted(
                surveyId,
                PLATFORM_ORGANIZATION_ID,
                evidenceId,
                rag.at("/data/answer").asText().replace("\"", "'"),
                objectMapper.writeValueAsString(rag.at("/data/citations"))), 200));
        String decisionId = decision.at("/data/id").asText();
        assertThat(decision.at("/data/recommendations").size()).isGreaterThanOrEqualTo(1);

        JsonNode report = json(postJson("/api/v1/reports", token, """
                {
                  "decisionId":"%s",
                  "surveyId":"%s",
                  "organizationId":"%s",
                  "reportType":"EXECUTIVE",
                  "title":"Recovery Executive Report"
                }
                """.formatted(decisionId, surveyId, PLATFORM_ORGANIZATION_ID), 201));
        String reportId = report.at("/data/id").asText();

        byte[] pdf = mockMvc.perform(get("/api/v1/reports/" + reportId + "/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertThat(new String(pdf, StandardCharsets.UTF_8)).startsWith("%PDF");

        String csv = mockMvc.perform(get("/api/v1/reports/" + reportId + "/csv")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(csv).contains("root_cause").contains("recommendation");
    }

    private String uploadEvidence(String token, String surveyId, String questionId) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "recovery-water.png", "image/png", "water evidence".getBytes(StandardCharsets.UTF_8));
        return mockMvc.perform(multipart("/api/v1/evidence")
                        .file(file)
                        .param("organizationId", PLATFORM_ORGANIZATION_ID.toString())
                        .param("surveyId", surveyId)
                        .param("questionId", questionId)
                        .param("title", "Recovery water evidence")
                        .param("description", "Evidence uploaded by the recovery flow")
                        .param("tags", "recovery")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String registerAdmin() throws Exception {
        return json(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"recovery.admin",
                                  "email":"recovery.admin@example.gov",
                                  "fullName":"Recovery Admin",
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

    private String postJson(String path, String token, String payload, int expectedStatus) throws Exception {
        return mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}
