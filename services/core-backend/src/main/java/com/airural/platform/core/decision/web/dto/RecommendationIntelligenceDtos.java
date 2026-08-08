/*
 * Purpose: Defines recommendation intelligence API contracts.
 * Why it exists: Clients need structured decision-support recommendations, option comparison, risks, resources, metrics, scheme matches, and human approval payloads.
 * Architecture fit: DTO boundary for the Decision Intelligence bounded context.
 */
package com.airural.platform.core.decision.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.*;

/** Namespace for recommendation intelligence DTO records. */
public final class RecommendationIntelligenceDtos {
    private RecommendationIntelligenceDtos() {}

    /** Request to generate recommendations from validated root causes and context. */
    public record RecommendationGenerateRequest(
            UUID rootCauseAnalysisId,
            List<RootCauseInput> validatedRootCauses,
            Map<String, Object> villageContext,
            List<Map<String, Object>> evidence,
            Map<String, Object> availableResources,
            Map<String, Object> constraints,
            String domain,
            Integer targetPopulation,
            String knowledgeSnapshot,
            String evidenceSnapshot,
            Boolean requireHumanApproval) {}

    /** Minimal root-cause input for callers that do not reference a stored analysis. */
    public record RootCauseInput(@NotBlank String rootCauseId, @NotBlank String description, String domain, Double confidence, List<String> evidence) {}

    /** Recommendation generation response. */
    public record RecommendationSetResponse(
            UUID recommendationSetId,
            UUID rootCauseAnalysisId,
            String status,
            List<RecommendationOptionResponse> options,
            List<OptionComparisonResponse> comparison,
            List<SchemeMatchResponse> schemeMatches,
            List<String> methodology,
            String model,
            String modelVersion,
            String promptVersion,
            String knowledgeSnapshot,
            String evidenceSnapshot,
            Instant createdAt) {}

    /** Intervention option linked to one or more root causes. */
    public record RecommendationOptionResponse(
            String recommendationId,
            String title,
            String description,
            String targetRootCause,
            Integer targetPopulation,
            String domain,
            String interventionType,
            Integer priority,
            List<String> expectedOutcomes,
            List<String> requiredResources,
            String estimatedEffort,
            String estimatedTimeframe,
            FeasibilityResponse feasibility,
            List<RiskResponse> risks,
            List<String> dependencies,
            List<String> evidence,
            ConfidenceBreakdownResponse confidence,
            List<String> assumptions,
            List<String> limitations,
            List<ImplementationPhaseResponse> implementationPlan,
            List<MetricResponse> successIndicators,
            String status) {}

    /** Option comparison result. */
    public record OptionComparisonResponse(String recommendationId, double priorityScore, List<String> advantages, List<String> disadvantages, String effortCategory, String complexity, List<String> unintendedConsequences, Map<String, Double> scoreConfig) {}

    /** Scheme matching result. */
    public record SchemeMatchResponse(String schemeName, String source, List<String> eligibilityEvidence, String applicablePopulation, String relevantBenefit, List<String> limitations, String sourceDateVersion, String status) {}

    /** Separate confidence dimensions. */
    public record ConfidenceBreakdownResponse(double evidenceStrength, double recommendationConfidence, double implementationFeasibility, String interpretation) {}

    /** Feasibility record. */
    public record FeasibilityResponse(String rating, List<String> supportingFactors, List<String> constraints, String resourceStatus) {}

    /** Risk record. */
    public record RiskResponse(String riskType, String description, String severity, String likelihood, String mitigation, String evidenceOrAssumption) {}

    /** Implementation phase record. */
    public record ImplementationPhaseResponse(String phase, List<String> actions, String responsibleRole, List<String> requiredInputs, List<String> dependencies, List<String> successIndicators) {}

    /** Success indicator record. */
    public record MetricResponse(String name, String baseline, String target, String measurementMethod, String measurementFrequency, String dataGap) {}

    /** Review request for generated recommendations. */
    public record RecommendationReviewRequest(@NotBlank String action, String reviewerNotes, Map<String, Object> modifiedRecommendation, String correction) {}

    /** Review response. */
    public record RecommendationReviewResponse(UUID reviewId, UUID recommendationSetId, String action, String status, Instant reviewedAt) {}
}
