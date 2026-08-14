/*
 * Purpose: Verifies the authenticated human-quality review workflow against the web boundary.
 * Why it exists: The held-out baseline must be scored by a human through protected APIs, not by automation or the training-review path.
 * Architecture fit: Integration coverage for evaluation storage, RBAC, immutable catalog loading, and rubric validation.
 */
package com.airural.platform.core.evaluation;

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
import java.util.Set;
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

/** API tests for authenticated human evaluation. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:airural_human_evaluation_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:h2-schemas.sql'",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class HumanEvaluationIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void ensureReviewerRole() {
        if (organizationRepository.findByCode("PLATFORM").isEmpty()) {
            organizationRepository.save(new OrganizationEntity("Platform Administration", "PLATFORM"));
        }
        if (roleRepository.findByName("ADMINISTRATOR").isEmpty()) {
            Set<PermissionEntity> permissions = Set.of(
                    permissionRepository.save(new PermissionEntity("AI_ADMIN", "AI", "ADMIN", "Test AI administration")),
                    permissionRepository.save(new PermissionEntity("EVALUATION_READ", "EVALUATION", "READ", "Test evaluation read")),
                    permissionRepository.save(new PermissionEntity("AI_GOVERNANCE_REVIEW", "EVALUATION", "REVIEW", "Test human evaluation review")));
            roleRepository.save(new RoleEntity("ADMINISTRATOR", "Test administrator", permissions));
        }
        if (roleRepository.findByName("FIELD_SURVEYOR").isEmpty()) {
            PermissionEntity read = permissionRepository.findByName("EVALUATION_READ")
                    .orElseGet(() -> permissionRepository.save(new PermissionEntity("EVALUATION_READ", "EVALUATION", "READ", "Test evaluation read")));
            PermissionEntity review = permissionRepository.findByName("AI_GOVERNANCE_REVIEW")
                    .orElseGet(() -> permissionRepository.save(new PermissionEntity("AI_GOVERNANCE_REVIEW", "EVALUATION", "REVIEW", "Test human evaluation review")));
            roleRepository.save(new RoleEntity("FIELD_SURVEYOR", "Test field surveyor", Set.of(read, review)));
        }
    }

    @Test
    void unauthenticatedReviewerCannotReadHeldOutExamples() throws Exception {
        mockMvc.perform(get("/api/v1/evaluation/human/examples"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserCannotSubmitHumanScores() throws Exception {
        mockMvc.perform(post("/api/v1/evaluation/human/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exampleId\":\"3d8e7942-43b9-4289-9901-ca706fdfb304\",\"scores\":{},\"evidenceReferencesUsed\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedReviewerCanScoreOneExampleAndDuplicateIsRejected() throws Exception {
        String token = registerFirstUser("human.reviewer@example.gov", "human.reviewer");

        JsonNode queue = objectMapper.readTree(mockMvc.perform(get("/api/v1/evaluation/human/examples")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(queue.at("/data/total").asInt()).isEqualTo(4);
        assertThat(queue.at("/data/remaining").asInt()).isEqualTo(4);

        String exampleId = queue.at("/data/examples/0/exampleId").asText();
        String payload = """
                {
                  "exampleId":"%s",
                  "scores":{
                    "rootCauseQuality":3,
                    "ragEvidenceQuality":3,
                    "uncertaintyHandling":4,
                    "practicalUsefulness":3
                  },
                  "evidenceReferencesUsed":["CONTROLLED_PROJECT_PILOT"],
                  "reviewerComments":"Human review captured for integration verification."
                }
                """.formatted(exampleId);

        JsonNode review = objectMapper.readTree(mockMvc.perform(post("/api/v1/evaluation/human/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(review.at("/data/status").asText()).isEqualTo("SUBMITTED");
        assertThat(review.at("/data/reviewerId").asText()).isNotBlank();

        JsonNode updated = objectMapper.readTree(mockMvc.perform(get("/api/v1/evaluation/human/examples")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(updated.at("/data/scored").asInt()).isEqualTo(1);
        assertThat(updated.at("/data/remaining").asInt()).isEqualTo(3);

        mockMvc.perform(post("/api/v1/evaluation/human/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void recommendationRequiresRecommendationScore() throws Exception {
        String token = registerFirstUser("recommendation.reviewer@example.gov", "recommendation.reviewer");
        mockMvc.perform(post("/api/v1/evaluation/human/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "exampleId":"4f1d260f-c355-4fe7-be2e-32a2973f6d68",
                                  "scores":{"rootCauseQuality":3,"ragEvidenceQuality":3,"uncertaintyHandling":3,"practicalUsefulness":3},
                                  "evidenceReferencesUsed":["CONTROLLED_PROJECT_PILOT"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private String registerFirstUser(String email, String username) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"%s",
                                  "email":"%s",
                                  "fullName":"Human Evaluation Reviewer",
                                  "password":"VeryStrongPassword123!",
                                  "organizationCode":"PLATFORM"
                                }
                                """.formatted(username, email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).at("/data/accessToken").asText();
    }
}
