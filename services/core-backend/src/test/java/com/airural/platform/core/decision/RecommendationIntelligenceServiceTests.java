/*
 * Purpose: Verifies recommendation intelligence generation behavior.
 * Why it exists: Recommendation decision support must stay grounded in validated root causes, separate confidence dimensions, surface missing resources, and require human approval.
 * Architecture fit: Unit-level coverage for the Decision Intelligence application service without requiring live Ollama, Qdrant, or PostgreSQL.
 */
package com.airural.platform.core.decision;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.decision.application.*;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcOperations;

/** Tests for root-cause-linked recommendation generation. */
@ExtendWith(MockitoExtension.class)
class RecommendationIntelligenceServiceTests {

    @Test
    void generatesPrioritizedOptionsFromValidatedRootCauseAndRagEvidence() {
        JdbcOperations jdbcTemplate = mock(JdbcOperations.class);
        RecommendationIntelligenceService service = service(jdbcTemplate, List.of(new CitationResponse("rag-water-scheme", "approved-water-policy", "Water asset repair and maintenance programs support source reliability, government schemes, and water infrastructure planning.", 0.86)));

        RecommendationSetResponse response = service.generate(request(Map.of("fieldTeam", "available")), UUID.randomUUID());

        assertThat(response.status()).isEqualTo("AI_GENERATED");
        assertThat(response.options()).hasSize(3);
        assertThat(response.options()).allSatisfy(option -> {
            assertThat(option.targetRootCause()).contains("Delayed bore well repair");
            assertThat(option.evidence()).contains("field-note-1", "approved-water-policy");
            assertThat(option.confidence().evidenceStrength()).isGreaterThan(0.0);
            assertThat(option.confidence().recommendationConfidence()).isGreaterThan(0.0);
            assertThat(option.confidence().implementationFeasibility()).isGreaterThan(0.0);
            assertThat(option.status()).isEqualTo("AI_GENERATED");
        });
        assertThat(response.comparison()).isSortedAccordingTo(Comparator.comparing(OptionComparisonResponse::priorityScore).reversed());
        assertThat(response.schemeMatches()).isNotEmpty();
        verify(jdbcTemplate, atLeastOnce()).update(anyString(), any(Object[].class));
    }

    @Test
    void flagsResourceInformationGapsInResourcesRisksAndLimitations() {
        RecommendationIntelligenceService service = service(mock(JdbcOperations.class), List.of(new CitationResponse("rag-water", "approved-water-policy", "Water asset repair and maintenance programs support source reliability.", 0.86)));

        RecommendationSetResponse response = service.generate(request(Map.of()), UUID.randomUUID());

        assertThat(response.schemeMatches()).isNotEmpty();
        assertThat(response.schemeMatches()).allMatch(match -> match.status().equals("ELIGIBILITY_REQUIRES_VERIFICATION"));
        assertThat(response.options()).allSatisfy(option -> {
            assertThat(option.requiredResources()).anyMatch(resource -> resource.contains("RESOURCE_INFORMATION_MISSING"));
            assertThat(option.risks()).anyMatch(risk -> risk.riskType().equals("Data Risk") && risk.severity().equals("HIGH"));
            assertThat(option.limitations()).contains("RESOURCE_INFORMATION_MISSING");
            assertThat(option.feasibility().resourceStatus()).isEqualTo("RESOURCE_INFORMATION_MISSING");
        });
    }

    @Test
    void rejectsGenerationWithoutValidatedRootCause() {
        RecommendationIntelligenceService service = service(mock(JdbcOperations.class), List.of());

        RecommendationGenerateRequest emptyRequest = new RecommendationGenerateRequest(null, List.of(), Map.of(), List.of(), Map.of(), Map.of(), "WATER", 120, "knowledge-test", "evidence-test", true);

        assertThatThrownBy(() -> service.generate(emptyRequest, UUID.randomUUID()))
                .isInstanceOf(DecisionException.class)
                .hasMessageContaining("At least one validated root cause");
    }

    @Test
    void filtersRecommendationEvidenceToPermittedScenarioSources() {
        JdbcOperations jdbcTemplate = mock(JdbcOperations.class);
        RecommendationIntelligenceService service = service(jdbcTemplate, List.of(
                new CitationResponse("rag-service", "permitted-source", "Approved crop disease evidence describes pest damage, soil and crop advisory, and treatment response.", 0.86),
                new CitationResponse("rag-service", "unrelated-source", "Unrelated evidence.", 0.85)));
        RecommendationGenerateRequest request = new RecommendationGenerateRequest(
                null,
                List.of(new RootCauseInput("rc-1", "Validated crop disease factor.", "AGRICULTURE", 0.82, List.of("permitted-source"))),
                Map.of(),
                List.of(),
                Map.of("fieldTeam", "available"),
                Map.of("allowed_source_ids", List.of("permitted-source")),
                "AGRICULTURE",
                10,
                "knowledge-test",
                "evidence-test",
                true);

        RecommendationSetResponse response = service.generate(request, UUID.randomUUID());

        assertThat(response.options()).allSatisfy(option -> {
            assertThat(option.evidence()).contains("permitted-source");
            assertThat(option.evidence()).doesNotContain("unrelated-source");
        });
    }

    @Test
    void rejectsRecommendationWhenPermittedCitationIsSemanticallyUnrelated() {
        RecommendationIntelligenceService service = service(mock(JdbcOperations.class), List.of(
                new CitationResponse("rag-service", "permitted-source", "Market logistics and buyer delivery records.", 0.86)));
        RecommendationGenerateRequest request = new RecommendationGenerateRequest(
                null,
                List.of(new RootCauseInput("rc-1", "Validated crop disease factor.", "AGRICULTURE", 0.82, List.of("permitted-source"))),
                Map.of(),
                List.of(),
                Map.of(),
                Map.of("allowed_source_ids", List.of("permitted-source")),
                "AGRICULTURE",
                10,
                "knowledge-test",
                "evidence-test",
                true);

        assertThatThrownBy(() -> service.generate(request, UUID.randomUUID()))
                .isInstanceOf(DecisionException.class)
                .hasMessageContaining("semantically relevant");
    }

    @Test
    void excludesGenericMarketRecommendationFromCropDiseaseEvidence() {
        RecommendationIntelligenceService service = service(mock(JdbcOperations.class), List.of(
                new CitationResponse("rag-service", "crop-source", "Crop disease and pest damage response should use field scouting and treatment verification.", 0.86)));
        RecommendationGenerateRequest request = new RecommendationGenerateRequest(
                null,
                List.of(new RootCauseInput("rc-1", "Validated crop disease factor.", "AGRICULTURE", 0.82, List.of("crop-source"))),
                Map.of(),
                List.of(),
                Map.of(),
                Map.of("allowed_source_ids", List.of("crop-source")),
                "AGRICULTURE",
                10,
                "knowledge-test",
                "evidence-test",
                true);

        RecommendationSetResponse response = service.generate(request, UUID.randomUUID());

        assertThat(response.options()).hasSize(2);
        assertThat(response.options()).allMatch(option -> !option.title().toLowerCase(Locale.ROOT).contains("market"));
    }

    @Test
    void hydratesDurableStatusWhenReadingGeneratedResponse() throws Exception {
        JdbcOperations jdbcTemplate = mock(JdbcOperations.class);
        RecommendationSetResponse generated = generatedResponse(jdbcTemplate);
        stubStoredResponse(jdbcTemplate, generated, "APPROVED");

        RecommendationSetResponse stored = service(jdbcTemplate, List.of()).get(generated.recommendationSetId());

        assertThat(stored.status()).isEqualTo("APPROVED");
        assertThat(stored.options()).hasSize(3);
    }

    @Test
    void appliesHumanEditAndCreatesNewRecommendationVersion() throws Exception {
        JdbcOperations jdbcTemplate = mock(JdbcOperations.class);
        RecommendationSetResponse generated = generatedResponse(jdbcTemplate);
        stubStoredResponse(jdbcTemplate, generated, "AI_GENERATED");
        Map<String, Object> editedOption = new ObjectMapper().convertValue(generated.options().get(0), Map.class);
        editedOption.put("title", "Human-reviewed water intervention");

        RecommendationReviewResponse review = service(jdbcTemplate, List.of()).review(
                generated.recommendationSetId(),
                new RecommendationReviewRequest("EDIT", "Reviewed by field lead", Map.of("options", List.of(editedOption)), "Updated title"),
                UUID.randomUUID());

        assertThat(review.action()).isEqualTo("EDIT");
        assertThat(review.status()).isEqualTo("UNDER_REVIEW");
        verify(jdbcTemplate, atLeast(2)).update(contains("insert into decision.recommendation_versions"), any(Object[].class));
        verify(jdbcTemplate).update(contains("response_json = ?::jsonb"), any(Object[].class));
    }

    @Test
    void rejectsEditWithoutModifiedOptions() throws Exception {
        JdbcOperations jdbcTemplate = mock(JdbcOperations.class);
        RecommendationSetResponse generated = generatedResponse(jdbcTemplate);
        stubStoredResponse(jdbcTemplate, generated, "AI_GENERATED");

        assertThatThrownBy(() -> service(jdbcTemplate, List.of()).review(
                generated.recommendationSetId(),
                new RecommendationReviewRequest("EDIT", "Missing edit", Map.of(), null),
                UUID.randomUUID()))
                .isInstanceOf(DecisionException.class)
                .hasMessageContaining("must include an options array");
    }

    private RecommendationSetResponse generatedResponse(JdbcOperations jdbcTemplate) {
        return service(jdbcTemplate, List.of(new CitationResponse("rag-water", "policy", "Water asset maintenance and repair records support reliable water service, government schemes, and infrastructure.", 0.9)))
                .generate(request(Map.of("fieldTeam", "available")), UUID.randomUUID());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubStoredResponse(JdbcOperations jdbcTemplate, RecommendationSetResponse response, String status) throws Exception {
        String responseJson = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);
        when(jdbcTemplate.queryForMap(anyString(), any(UUID.class)))
                .thenReturn(Map.of("response_json", responseJson, "status", status));
    }

    private RecommendationIntelligenceService service(JdbcOperations jdbcTemplate, List<CitationResponse> citations) {
        RootCauseRagClient ragClient = (request, userId) -> new RagQueryResponse(UUID.randomUUID(), "Grounded recommendation context", citations, 5L, 14L);
        return new RecommendationIntelligenceService(jdbcTemplate, new ObjectMapper().findAndRegisterModules(), null, ragClient);
    }

    private RecommendationGenerateRequest request(Map<String, Object> resources) {
        RootCauseInput cause = new RootCauseInput(
                "rc-1",
                "Delayed bore well repair causes recurring household water shortage.",
                "WATER",
                0.82,
                List.of("field-note-1"));
        return new RecommendationGenerateRequest(
                null,
                List.of(cause),
                Map.of("village", "Rampur", "district", "Test District"),
                List.of(Map.of("id", "field-note-1", "type", "FIELD_NOTE")),
                resources,
                Map.of("humanApprovalRequired", true),
                "WATER",
                120,
                "knowledge-test",
                "evidence-test",
                true);
    }
}
