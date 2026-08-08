/*
 * Purpose: Verifies the root-cause intelligence engine contract.
 * Why it exists: The engine must preserve fact/inference separation, contradictions, uncertainty, causal graph, and grounded evidence behavior.
 * Architecture fit: Unit-level coverage for the Decision Intelligence application service without requiring live Ollama or Qdrant.
 */
package com.airural.platform.core.decision;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.decision.application.RootCauseIntelligenceService;
import com.airural.platform.core.decision.application.RootCauseRagClient;
import com.airural.platform.core.decision.web.dto.RootCauseDtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcOperations;

/** Tests for transparent root-cause analysis generation. */
@ExtendWith(MockitoExtension.class)
class RootCauseIntelligenceServiceTests {

    @Test
    void analysisSeparatesFactsEvidenceHypothesesAndCausalGraph() {
        RootCauseIntelligenceService service = serviceWithRag(List.of(new CitationResponse("rag-service", "approved-water-policy", "Bore well downtime and delayed maintenance may contribute to rural water reliability issues.", 0.82)));
        RootCauseAnalysisResponse response = service.analyze(waterRequest(false), UUID.randomUUID());

        assertThat(response.observedFacts()).extracting(FactResponse::category).contains("OBSERVED_FACT", "RETRIEVED_EVIDENCE");
        assertThat(response.contributingFactors()).isNotEmpty();
        assertThat(response.candidateRootCauses()).isNotEmpty();
        assertThat(response.alternativeHypotheses()).isNotEmpty();
        assertThat(response.causalGraph()).extracting(CausalRelationshipResponse::relationshipType).contains("FACTOR_ASSOCIATED_WITH_OUTCOME");
        assertThat(response.confidence().interpretation()).contains("not a calibrated scientific probability");
    }

    @Test
    void analysisFlagsContradictoryEvidence() {
        RootCauseIntelligenceService service = serviceWithRag(List.of());
        RootCauseAnalysisResponse response = service.analyze(waterRequest(true), UUID.randomUUID());

        assertThat(response.uncertainties()).anyMatch(item -> item.uncertaintyId().equals("uncertainty-contradictions"));
        assertThat(response.limitations()).anyMatch(item -> item.contains("Contradictory evidence"));
    }

    @Test
    void analysisReportsInsufficientEvidenceWhenFactsAreSparse() {
        RootCauseIntelligenceService service = serviceWithRag(List.of());
        RootCauseAnalysisResponse response = service.analyze(new RootCauseAnalysisRequest(
                new ProblemRequest("p-low-data", "Rampur", "Employment", "Youth unemployment has increased.", 40, "MEDIUM", List.of(), null, "synthetic-test"),
                List.of(),
                List.of(),
                Map.of(),
                List.of(),
                null,
                null,
                "synthetic-v1",
                "test-snapshot",
                true), UUID.randomUUID());

        assertThat(response.uncertainties()).anyMatch(item -> item.uncertaintyId().equals("uncertainty-evidence-volume"));
        assertThat(response.followUpQuestions()).isNotEmpty();
        assertThat(response.validatedRootCauses()).isEmpty();
    }

    private RootCauseIntelligenceService serviceWithRag(List<CitationResponse> citations) {
        JdbcOperations jdbcTemplate = mock(JdbcOperations.class);
        RootCauseRagClient ragClient = (request, userId) -> new RagQueryResponse(UUID.randomUUID(), "Grounded RAG answer", citations, 5L, 10L);
        return new RootCauseIntelligenceService(jdbcTemplate, new ObjectMapper().findAndRegisterModules(), ragClient);
    }

    private RootCauseAnalysisRequest waterRequest(boolean contradiction) {
        List<Map<String, Object>> survey = new ArrayList<>();
        survey.add(Map.of("main_problem", "Water shortage during summer", "repair_time", "Pump repair is delayed for more than ten days"));
        if (contradiction) {
            survey.add(Map.of("water_availability", "Water is available throughout the year"));
        }
        List<Map<String, Object>> evidence = List.of(Map.of("field_note", contradiction ? "Village experiences severe water shortages during summer" : "Bore well downtime is frequent during summer"));
        return new RootCauseAnalysisRequest(
                new ProblemRequest("p-water", "Rampur", "Water", "Village water reliability failures affect households.", 120, "HIGH", List.of("survey", "field-note"), null, "synthetic-test"),
                survey,
                evidence,
                Map.of("households_affected", 120),
                List.of(),
                null,
                null,
                "synthetic-v1",
                "test-snapshot",
                true);
    }
}
