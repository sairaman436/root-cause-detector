/*
 * Purpose: Verifies survey APIs, persistence, RBAC, workflow, search, and validation behavior end to end.
 * Why it exists: Milestone 3 requires production-grade integration coverage for the survey module.
 * Architecture fit: API and persistence integration tests against the Spring Boot backend.
 */
package com.airural.platform.core.survey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for Enterprise Survey Management. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:airural_survey_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class SurveyIntegrationTests {
    private static final UUID PLATFORM_ORGANIZATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Survey APIs support create, questionnaire, search, assignment, clone, and workflow operations. */
    @Test
    void surveyManagementFlowWorksThroughSecuredApis() throws Exception {
        String token = registerAdmin();

        UUID templateId = UUID.fromString(json(postJson("/api/v1/survey-templates", token, """
                {
                  "name": "Water Reliability Template",
                  "description": "Template for water infrastructure root-cause discovery",
                  "category": "WATER",
                  "status": "APPROVED",
                  "metadataJson": "{\\"source\\":\\"milestone-3\\"}"
                }
                """)).at("/data/id").asText());

        UUID surveyId = UUID.fromString(json(postJson("/api/v1/surveys", token, """
                {
                  "templateId": "%s",
                  "organizationId": "%s",
                  "name": "Village Water Reliability Survey",
                  "description": "Collects structured water reliability evidence",
                  "tags": ["water", "infrastructure"]
                }
                """.formatted(templateId, PLATFORM_ORGANIZATION_ID))).at("/data/id").asText());

        UUID sectionId = UUID.fromString(json(postJson("/api/v1/surveys/" + surveyId + "/sections", token, """
                {
                  "code": "water_access",
                  "title": "Water Access",
                  "description": "Water access conditions",
                  "orderIndex": 1,
                  "repeatable": false
                }
                """)).at("/data/id").asText());

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
                """.formatted(sectionId)));
        assertThat(question.at("/data/questionType").asText()).isEqualTo("SINGLE_SELECT");
        assertThat(question.at("/data/options").size()).isEqualTo(2);

        JsonNode search = json(getJson("/api/v1/surveys?name=Village&tag=water&status=DRAFT", token));
        assertThat(search.at("/data/content").size()).isEqualTo(1);

        JsonNode assignment = json(postJson("/api/v1/surveys/" + surveyId + "/assignments", token, """
                {
                  "assignmentType": "ORGANIZATION",
                  "targetId": "%s",
                  "targetName": "Platform Administration"
                }
                """.formatted(PLATFORM_ORGANIZATION_ID)));
        assertThat(assignment.at("/data/assignmentType").asText()).isEqualTo("ORGANIZATION");

        JsonNode clone = json(postJson("/api/v1/surveys/" + surveyId + "/clone", token, """
                {
                  "organizationId": "%s",
                  "name": "Village Water Reliability Survey Copy",
                  "description": "Copied survey",
                  "tags": ["water"]
                }
                """.formatted(PLATFORM_ORGANIZATION_ID)));
        assertThat(clone.at("/data/clonedFromSurveyId").asText()).isEqualTo(surveyId.toString());

        for (String status : new String[] {"REVIEW", "APPROVED", "PUBLISHED"}) {
            JsonNode transition = json(postJson("/api/v1/surveys/" + surveyId + "/workflow", token, """
                    {"status": "%s", "reason": "integration test transition"}
                    """.formatted(status)));
            assertThat(transition.at("/data/status").asText()).isEqualTo(status);
        }

        postJsonExpect("/api/v1/surveys/" + surveyId + "/sections", token, """
                {
                  "code": "locked_section",
                  "title": "Locked Section",
                  "orderIndex": 99
                }
                """, status().isConflict());

        for (String status : new String[] {"ACTIVE", "COMPLETED", "ARCHIVED"}) {
            JsonNode transition = json(postJson("/api/v1/surveys/" + surveyId + "/workflow", token, """
                    {"status": "%s", "reason": "integration test transition"}
                    """.formatted(status)));
            assertThat(transition.at("/data/status").asText()).isEqualTo(status);
        }

        JsonNode versions = json(getJson("/api/v1/surveys/" + surveyId + "/versions", token));
        assertThat(versions.at("/data").size()).isGreaterThanOrEqualTo(1);
    }

    /** Invalid workflow jumps and invalid validation rules fail with API errors. */
    @Test
    void validationAndWorkflowFailuresReturnApiErrors() throws Exception {
        String token = registerAdmin("survey.failure.admin@example.gov", "survey.failure.admin");
        UUID surveyId = UUID.fromString(json(postJson("/api/v1/surveys", token, """
                {
                  "organizationId": "%s",
                  "name": "Validation Failure Survey",
                  "tags": ["validation"]
                }
                """.formatted(PLATFORM_ORGANIZATION_ID))).at("/data/id").asText());

        postJsonExpect("/api/v1/surveys/" + surveyId + "/workflow", token, "{\"status\":\"ACTIVE\"}", status().isConflict());
        postJsonExpect("/api/v1/surveys/" + surveyId + "/validation-rules", token, """
                {
                  "ruleType": "REGEX",
                  "expression": "[",
                  "message": "Invalid regex",
                  "orderIndex": 1
                }
                """, status().isBadRequest());
    }

    private String registerAdmin() throws Exception {
        return registerAdmin("survey.admin@example.gov", "survey.admin");
    }

    private String registerAdmin(String email, String username) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "email": "%s",
                  "fullName": "Survey Admin",
                  "password": "VeryStrongPassword123!",
                  "organizationCode": "PLATFORM"
                }
                """.formatted(username, email);
        return json(postJson("/api/v1/auth/register", null, payload)).at("/data/accessToken").asText();
    }

    private String postJson(String path, String token, String payload) throws Exception {
        return mockMvc.perform(post(path)
                        .header("Authorization", token == null ? "" : "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String getJson(String path, String token) throws Exception {
        return mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void postJsonExpect(String path, String token, String payload, org.springframework.test.web.servlet.ResultMatcher matcher) throws Exception {
        mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(matcher);
    }

    private JsonNode json(String response) throws Exception {
        return objectMapper.readTree(response);
    }
}
