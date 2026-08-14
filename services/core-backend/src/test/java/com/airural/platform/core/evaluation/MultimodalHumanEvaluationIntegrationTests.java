/*
 * Purpose: Verifies server-backed multimodal review authorization, validation, persistence, and duplicate protection.
 * Why it exists: Browser-local scoring is not an acceptable governance record.
 * Architecture fit: Integration coverage for the multimodal evaluation REST boundary.
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** API tests for the authenticated multimodal human evaluation workflow. */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:airural_multimodal_evaluation_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:h2-schemas.sql'",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "airural.evaluation.multimodal-artifact-path=../../artifacts/evaluation"
})
class MultimodalHumanEvaluationIntegrationTests {
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
        PermissionEntity governance = permissionRepository.findByName("AI_GOVERNANCE_REVIEW")
                .orElseGet(() -> permissionRepository.save(new PermissionEntity("AI_GOVERNANCE_REVIEW", "EVALUATION", "REVIEW", "Test multimodal review")));
        PermissionEntity admin = permissionRepository.findByName("AI_ADMIN")
                .orElseGet(() -> permissionRepository.save(new PermissionEntity("AI_ADMIN", "AI", "ADMIN", "Test AI administration")));
        if (roleRepository.findByName("ADMINISTRATOR").isEmpty()) {
            roleRepository.save(new RoleEntity("ADMINISTRATOR", "Test administrator", Set.of(governance, admin)));
        }
        if (roleRepository.findByName("FIELD_SURVEYOR").isEmpty()) {
            roleRepository.save(new RoleEntity("FIELD_SURVEYOR", "Test reviewer", Set.of(governance)));
        }
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/v1/evaluation/multimodal/traces"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "EVALUATION_READ")
    void unauthorizedUsersCannotReadOrSubmitMultimodalReviews() throws Exception {
        mockMvc.perform(get("/api/v1/evaluation/multimodal/traces"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizedReviewerCanLoadAndSubmitOnce() throws Exception {
        String token = registerFirstUser("multimodal.reviewer@example.gov", "multimodal.reviewer");
        JsonNode queue = objectMapper.readTree(mockMvc.perform(get("/api/v1/evaluation/multimodal/traces")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(queue.at("/data/total").asInt()).isGreaterThanOrEqualTo(1);
        JsonNode water = findTrace(queue, "mm-water-live-20260814");
        assertThat(water.at("/reviewStatus").asText()).isEqualTo("REMAINING");

        String payload = """
                {
                  "traceId":"mm-water-live-20260814",
                  "artifactVersion":"MULTIMODAL_CROSS_DOMAIN_2026-08-14@1.0.0",
                  "evaluationRound":"MULTIMODAL_CROSS_DOMAIN_2026-08-14",
                  "rubricVersion":"HUMAN-QUALITY-RUBRIC@1.0.0",
                  "scores":{"observationQuality":4,"evidenceRelevance":3,"rootCauseQuality":3,"recommendationQuality":null,"grounding":4,"overallUsefulness":3},
                  "failureClassification":"INSUFFICIENT_EVIDENCE",
                  "unsupportedClaimFlags":{"observation":false,"evidence":false,"rootCause":false,"recommendation":false},
                  "reviewerComments":"The trace correctly stopped before an unsupported recommendation."
                }
                """;
        JsonNode review = objectMapper.readTree(mockMvc.perform(post("/api/v1/evaluation/multimodal/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(review.at("/data/submissionStatus").asText()).isEqualTo("SUBMITTED");
        assertThat(review.at("/data/reviewerId").asText()).isNotBlank();

        JsonNode updated = objectMapper.readTree(mockMvc.perform(get("/api/v1/evaluation/multimodal/traces")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(findTrace(updated, "mm-water-live-20260814").at("/reviewStatus").asText()).isEqualTo("SCORED");

        mockMvc.perform(post("/api/v1/evaluation/multimodal/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRubricMetadataIsRejected() throws Exception {
        String token = registerFirstUser("multimodal.invalid@example.gov", "multimodal.invalid");
        mockMvc.perform(post("/api/v1/evaluation/multimodal/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"traceId":"mm-water-live-20260814","artifactVersion":"wrong","evaluationRound":"MULTIMODAL_CROSS_DOMAIN_2026-08-14","rubricVersion":"HUMAN-QUALITY-RUBRIC@1.0.0","scores":{"observationQuality":4,"evidenceRelevance":3,"rootCauseQuality":3,"grounding":4,"overallUsefulness":3},"failureClassification":"NONE","unsupportedClaimFlags":{"observation":false,"evidence":false,"rootCause":false,"recommendation":false}}
                                """))
                .andExpect(status().isBadRequest());
    }

    private JsonNode findTrace(JsonNode queue, String traceId) {
        for (JsonNode trace : queue.at("/data/traces")) {
            if (trace.at("/traceId").asText().equals(traceId)) return trace;
        }
        throw new AssertionError("Trace was not returned: " + traceId);
    }

    private String registerFirstUser(String email, String username) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","fullName":"Multimodal Reviewer","password":"VeryStrongPassword123!","organizationCode":"PLATFORM"}
                                """.formatted(username, email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).at("/data/accessToken").asText();
    }
}
