/*
 * Purpose: Defines the response contract for the first governed development evaluation.
 * Why it exists: Operators need durable IDs and provenance status without exposing mutable
 * evaluation internals or implying that synthetic development data is production evidence.
 * Architecture fit: REST boundary for the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.web.dto;

import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

/** DTO namespace for governed evaluation execution. */
public final class GovernedEvaluationDtos {
    private GovernedEvaluationDtos() {}

    /** Identifiers and status returned after one development-only pipeline execution. */
    public record GovernedEvaluationResponse(
            UUID pilotRunId,
            UUID scenarioId,
            UUID evaluationResultId,
            UUID rootCauseAnalysisId,
            UUID recommendationSetId,
            String status,
            String evaluationBasis,
            String provenanceStatus,
            boolean qwenFallbackUsed,
            int ragCitationCount,
            int recommendationOptionCount) {}

    /** Batch response for the fixed controlled pilot evaluation set. */
    public record GovernedEvaluationBatchResponse(
            List<GovernedEvaluationResponse> results,
            int completedScenarios,
            String reviewStatus) {}

    /** Result of deriving one pending recommendation evaluation from an approved root cause. */
    public record RecommendationCoverageCandidateResponse(
            UUID sourceCandidateId,
            String sourceScenario,
            String domain,
            String status,
            UUID evaluationResultId,
            UUID recommendationSetId,
            BigDecimal qualityScore,
            List<String> sourceIds,
            Integer outputTokenEstimate,
            String blockingReason) {}

    /** Batch result for governed recommendation coverage generation. */
    public record RecommendationCoverageBatchResponse(
            List<RecommendationCoverageCandidateResponse> candidates,
            int generated,
            int blocked,
            String reviewStatus) {}
}
