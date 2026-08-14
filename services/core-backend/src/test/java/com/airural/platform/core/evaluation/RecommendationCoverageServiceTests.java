/*
 * Purpose: Verifies the recommendation-coverage governance boundary.
 * Why it exists: A recommendation must never be generated from a pending or synthetic root-cause record.
 * Architecture fit: Unit coverage for the evaluation-to-learning handoff.
 */
package com.airural.platform.core.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.airural.platform.core.ai.application.AiFoundationService;
import com.airural.platform.core.decision.application.RecommendationIntelligenceService;
import com.airural.platform.core.evaluation.application.RecommendationCoverageService;
import com.airural.platform.core.evaluation.infrastructure.*;
import com.airural.platform.core.knowledge.application.KnowledgeRagGatewayService;
import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import com.airural.platform.core.learning.infrastructure.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcOperations;

/** Tests source validation before the recommendation pipeline is invoked. */
@ExtendWith(MockitoExtension.class)
class RecommendationCoverageServiceTests {
    @Mock private PilotDatasetRepository datasets;
    @Mock private PilotRunRepository runs;
    @Mock private PilotScenarioRepository scenarios;
    @Mock private PilotScenarioResultRepository results;
    @Mock private TrainingCandidateRepository candidates;
    @Mock private LearningRecordRepository learningRecords;
    @Mock private RecommendationIntelligenceService recommendations;
    @Mock private AiFoundationService ai;
    @Mock private KnowledgeRagGatewayService rag;
    @Mock private JdbcOperations jdbc;

    private RecommendationCoverageService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationCoverageService(datasets, runs, scenarios, results, candidates, learningRecords, recommendations, ai, rag, jdbc, new ObjectMapper());
    }

    @Test
    void blocksPendingRootCauseWithoutInvokingRecommendationGeneration() {
        UUID candidateId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        when(candidates.findById(candidateId)).thenReturn(java.util.Optional.of(candidate(candidateId, recordId, "PENDING_APPROVAL", false)));

        var outcome = service.generateOne(candidateId, UUID.randomUUID());

        assertEquals("BLOCKED", outcome.status());
        assertEquals("ROOT_CAUSE_NOT_HUMAN_VALIDATED", outcome.blockingReason());
        verifyNoInteractions(learningRecords, recommendations, ai, rag, datasets, runs, scenarios, results, jdbc);
    }

    @Test
    void blocksSyntheticApprovedRoot() {
        UUID candidateId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        when(candidates.findById(candidateId)).thenReturn(java.util.Optional.of(candidate(candidateId, recordId, "APPROVED_FOR_DATASET", true)));

        var outcome = service.generateOne(candidateId, UUID.randomUUID());

        assertEquals("BLOCKED", outcome.status());
        assertEquals("SYNTHETIC_ROOT_CANDIDATE", outcome.blockingReason());
        verifyNoInteractions(learningRecords, recommendations, ai, rag, datasets, runs, scenarios, results, jdbc);
    }

    @Test
    void missingCandidateIsBlockedWithoutCreatingReviewState() {
        UUID candidateId = UUID.randomUUID();
        when(candidates.findById(candidateId)).thenReturn(java.util.Optional.empty());

        var outcome = service.generateOne(candidateId, UUID.randomUUID());

        assertEquals("BLOCKED", outcome.status());
        assertEquals("ROOT_CANDIDATE_NOT_FOUND", outcome.blockingReason());
        verify(candidates, never()).save(any());
        verifyNoInteractions(learningRecords, recommendations, ai, rag, datasets, runs, scenarios, results, jdbc);
    }

    private TrainingCandidateEntity candidate(UUID candidateId, UUID recordId, String status, boolean synthetic) {
        TrainingCandidateEntity candidate = new TrainingCandidateEntity(
                candidateId,
                recordId,
                "dataset-v0.5",
                "EVALUATION_RESULT",
                BigDecimal.ONE,
                null,
                "governed",
                "PENDING_HUMAN_REVIEW",
                status,
                Instant.now(),
                null,
                null,
                synthetic);
        if ("APPROVED_FOR_DATASET".equals(status)) {
            candidate.review("APPROVE", status, "READY_FOR_DATASET", UUID.randomUUID(), "dataset-v0.5");
        }
        return candidate;
    }
}
