/*
 * Purpose: Verifies the authenticated candidate review and dataset export API flow.
 * Why it exists: Real learning records must pass JWT review, audit-backed candidate state, and approved-real export gates before dataset validation.
 * Architecture fit: End-to-end API coverage for the AI-7 to MLOps dataset boundary.
 */
package com.airural.platform.core.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.airural.platform.core.identity.domain.OrganizationEntity;
import com.airural.platform.core.identity.domain.PermissionEntity;
import com.airural.platform.core.identity.domain.RoleEntity;
import com.airural.platform.core.identity.infrastructure.OrganizationRepository;
import com.airural.platform.core.identity.infrastructure.PermissionRepository;
import com.airural.platform.core.identity.infrastructure.RoleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.airural.platform.core.learning.domain.LearningRecordEntity;
import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import com.airural.platform.core.learning.infrastructure.LearningRecordRepository;
import com.airural.platform.core.learning.infrastructure.TrainingCandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

/** API tests for governed training review. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:airural_training_review_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:h2-schemas.sql'",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class TrainingReviewIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private LearningRecordRepository learningRecordRepository;
    @Autowired private TrainingCandidateRepository trainingCandidateRepository;

    @BeforeEach
    void ensurePlatformOrganization() {
        if (organizationRepository.findByCode("PLATFORM").isEmpty()) {
            organizationRepository.save(new OrganizationEntity("Platform Administration", "PLATFORM"));
        }
        if (roleRepository.findByName("ADMINISTRATOR").isEmpty()) {
            var permissions = java.util.Set.of(
                    permissionRepository.save(new PermissionEntity("AI_ADMIN", "AI", "ADMIN", "Test AI governance administrator")),
                    permissionRepository.save(new PermissionEntity("LEARNING_CAPTURE", "LEARNING", "CAPTURE", "Test learning capture permission")),
                    permissionRepository.save(new PermissionEntity("LEARNING_REVIEW", "LEARNING", "REVIEW", "Test learning review permission")),
                    permissionRepository.save(new PermissionEntity("DATASET_APPROVAL", "DATASET", "APPROVE", "Test dataset approval permission")));
            roleRepository.save(new RoleEntity("ADMINISTRATOR", "Test administrator", permissions));
        }
    }

    @Test
    void unauthenticatedUserCannotReviewCandidateQueue() throws Exception {
        mockMvc.perform(get("/api/v1/learning/candidates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotReviewCandidate() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                        "/api/v1/learning/candidates/00000000-0000-0000-0000-000000000099/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void unauthorizedUserCannotReviewCandidate() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/learning/candidates/00000000-0000-0000-0000-000000000099/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotGenerateTrainingCandidates() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/learning/candidates/generate-from-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "LEARNING_REVIEW")
    void authorizedReviewerReceivesRemediationCandidatesFromQueue() throws Exception {
        for (int index = 0; index < 11; index++) {
            UUID recordId = UUID.randomUUID();
            learningRecordRepository.save(new LearningRecordEntity(
                    recordId,
                    Instant.now(),
                    "EVALUATION_RESULT",
                    "qwen2.5:0.5b",
                    "pilot-v05r@1.0.0",
                    "Scenario-specific governed context",
                    "Remediation input " + index,
                    "{\"answer\":\"pending\"}",
                    null,
                    null,
                    BigDecimal.valueOf(0.95),
                    "[{\"source_id\":\"PILOT_V05R_TEST_" + index + "\"}]",
                    "Evaluation Pipeline",
                    null,
                    false,
                    "INTERNAL",
                    "PENDING_HUMAN_REVIEW",
                    index % 3 == 0 ? "root-cause-analysis" : index % 3 == 1 ? "recommendation-generation" : "rag-grounded-responses",
                    "pilot-v05r-regression-" + index,
                    false,
                    null,
                    UUID.randomUUID(),
                    BigDecimal.valueOf(0.95),
                    "{\"classification\":\"PILOT_EVALUATION\"}"));
            trainingCandidateRepository.save(new TrainingCandidateEntity(
                    UUID.randomUUID(),
                    recordId,
                    "dataset-v0.5",
                    "EVALUATION_RESULT",
                    BigDecimal.valueOf(0.95),
                    "evaluation-pipeline",
                    "pilot-v05r-regression-" + index,
                    "PENDING_DATASET_APPROVAL",
                    "PENDING_APPROVAL",
                    Instant.now(),
                    null,
                    null,
                    false));
        }

        JsonNode response = objectMapper.readTree(mockMvc.perform(get("/api/v1/learning/candidates?size=50"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        JsonNode content = response.at("/data/content");
        assertThat(content).hasSize(11);
        for (JsonNode candidate : content) {
            assertThat(candidate.at("/approvalStatus").asText()).isEqualTo("PENDING_APPROVAL");
            assertThat(candidate.at("/scenarioGroup").asText()).startsWith("pilot-v05r-regression-");
        }
    }

    @Test
    void authenticatedCandidateCanBeCorrectedAndExported() throws Exception {
        String token = registerAdmin("training.review@example.gov", "training.review");
        JsonNode feedback = post(token, "/api/v1/learning/feedback", """
                {
                  "sourceType":"root-cause-analysis",
                  "taskType":"root-cause-analysis",
                  "scenarioGroup":"real-water-case-1",
                  "modelVersion":"qwen2.5:0.5b",
                  "promptVersion":"root-cause@1",
                  "retrievedContext":"Approved water policy context",
                  "input":"What should the officer verify?",
                  "aiOutput":"Verify the work order and field evidence.",
                  "evidenceUsedJson":"[{\\\"source_id\\\":\\\"policy-1\\\",\\\"valid\\\":true,\\\"supports_claim\\\":true}]",
                  "privacyClassification":"INTERNAL"
                }
                """);
        String recordId = feedback.at("/data/id").asText();

        JsonNode candidateDecision = post(token, "/api/v1/learning/review", """
                {
                  "learningRecordId":"%s",
                  "reviewer":"ignored-request-field",
                  "decision":"APPROVE",
                  "comments":"Initial quality review",
                  "createTrainingCandidate":true
                }
                """.formatted(recordId));
        String candidateId = candidateDecision.at("/data/id").asText();
        assertThat(candidateId).isNotBlank();

        JsonNode review = post(token, "/api/v1/learning/candidates/" + candidateId + "/review", """
                {
                  "decision":"CORRECT",
                  "correctedOutput":"Verify the work order, field evidence, and accountable maintenance assignment.",
                  "comments":"Correction preserves the original answer and adds the missing accountable assignment check."
                }
                """);
        assertThat(review.at("/data/status").asText()).isEqualTo("APPROVED_FOR_DATASET");

        JsonNode export = objectMapper.readTree(mockMvc.perform(get("/api/v1/learning/dataset-export").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(export.at("/data/status").asText()).isEqualTo("READY_FOR_JSONL_VALIDATION");
        assertThat(export.at("/data/examples")).hasSize(1);
        assertThat(export.at("/data/examples/0/output").asText()).contains("accountable maintenance assignment");
        assertThat(export.at("/data/examples/0/reviewDecision").asText()).isEqualTo("CORRECT");
    }

    private JsonNode post(String token, String path, String payload) throws Exception {
        return objectMapper.readTree(mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private String registerAdmin(String email, String username) throws Exception {
        return objectMapper.readTree(mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"%s",
                                  "email":"%s",
                                  "fullName":"Training Reviewer",
                                  "password":"VeryStrongPassword123!",
                                  "organizationCode":"PLATFORM"
                                }
                                """.formatted(username, email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).at("/data/accessToken").asText();
    }
}
