/*
 * Purpose: Defines REST contracts for decision intelligence operations.
 * Why it exists: Clients need stable DTOs for analysis, root-cause discovery, recommendations, history, explanations, and confidence.
 * Architecture fit: Web adapter contracts for Milestone 10 Decision Intelligence.
 */
package com.airural.platform.core.decision.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

/** Namespace for decision API DTO records. */
public final class DecisionDtos {
    private DecisionDtos() {}

    /** Decision analysis request. */
    public record DecisionAnalyzeRequest(UUID surveyId, UUID organizationId, List<UUID> evidenceIds, String problemStatement, Map<String, Object> surveyEvidence, Map<String, Object> mlPredictions, Map<String, Object> agentOutputs, Boolean requireHumanApproval) {}

    /** Root-cause request. */
    public record RootCauseRequest(UUID surveyId, UUID organizationId, @NotBlank String problemStatement, Map<String, Object> evidenceContext) {}

    /** Recommendation request. */
    public record RecommendationRequest(UUID decisionId, UUID surveyId, UUID organizationId, @NotBlank String objective, Map<String, Object> context) {}

    /** Decision response. */
    public record DecisionResponse(UUID id, String status, String decisionType, String finalDecision, Double confidence, Boolean humanApprovalRequired, List<RootCauseResponse> rootCauses, List<RecommendationResponse> recommendations, List<String> citations, Instant createdAt) {}

    /** Root-cause response. */
    public record RootCauseResponse(UUID id, String title, String description, Integer rank, Double confidence, String evidenceJson) {}

    /** Hypothesis response. */
    public record HypothesisResponse(UUID id, String title, String rationale, Double confidence, Integer rank, Boolean alternative) {}

    /** Recommendation response. */
    public record RecommendationResponse(UUID id, String title, String description, Integer priority, Double impactScore, Double confidence, Boolean humanApprovalRequired) {}

    /** Explanation response. */
    public record ExplanationResponse(UUID decisionId, List<DecisionTraceResponse> reasoningTrace, List<HypothesisResponse> hypotheses, List<RootCauseResponse> rootCauses, List<RecommendationResponse> recommendations, List<String> citations) {}

    /** Decision trace response. */
    public record DecisionTraceResponse(UUID id, String stepName, String detailsJson, Double confidenceAfterStep) {}

    /** Confidence response. */
    public record ConfidenceResponse(UUID id, Double overallConfidence, List<String> reasonCodes, List<String> missingEvidence, List<String> requiredFollowups) {}
}
