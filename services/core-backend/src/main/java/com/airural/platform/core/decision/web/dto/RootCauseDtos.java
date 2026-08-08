/*
 * Purpose: Defines structured root-cause intelligence API contracts.
 * Why it exists: Clients need fact/inference-separated analysis, causal graph, evidence scoring, and human review payloads.
 * Architecture fit: DTO boundary for the Decision Intelligence bounded context.
 */
package com.airural.platform.core.decision.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.*;

/** Namespace for root-cause intelligence DTO records. */
public final class RootCauseDtos {
    private RootCauseDtos() {}

    /** Root-cause analysis request. */
    public record RootCauseAnalysisRequest(
            ProblemRequest problem,
            List<Map<String, Object>> surveyResponses,
            List<Map<String, Object>> evidence,
            Map<String, Object> structuredData,
            List<Map<String, Object>> retrievedDocuments,
            UUID surveyId,
            UUID organizationId,
            String surveyVersion,
            String knowledgeSnapshot,
            Boolean requireHumanReview) {}

    /** Problem representation provided by the caller. */
    public record ProblemRequest(
            String problemId,
            String village,
            @NotBlank String domain,
            @NotBlank String description,
            Integer affectedPopulation,
            String severity,
            List<String> evidence,
            Instant timestamp,
            String source) {}

    /** Human review request. */
    public record HumanReviewRequest(
            @NotBlank String action,
            String reviewerNotes,
            Map<String, Object> modifiedAnalysis,
            List<Map<String, Object>> additionalEvidence,
            String correction) {}

    /** Root-cause analysis response matching the persistence schema. */
    public record RootCauseAnalysisResponse(
            UUID analysisId,
            Integer versionNumber,
            ProblemResponse problem,
            List<FactResponse> observedFacts,
            List<FactorResponse> contributingFactors,
            List<CandidateRootCauseResponse> candidateRootCauses,
            List<CandidateRootCauseResponse> validatedRootCauses,
            List<AlternativeHypothesisResponse> alternativeHypotheses,
            List<EvidenceAssessmentResponse> evidence,
            List<UncertaintyResponse> uncertainties,
            ConfidenceResponse confidence,
            List<String> limitations,
            List<String> followUpQuestions,
            List<CausalRelationshipResponse> causalGraph,
            String model,
            String modelVersion,
            String promptVersion,
            String knowledgeSnapshot,
            String surveyVersion,
            Instant createdAt) {}

    /** Problem representation emitted by the engine. */
    public record ProblemResponse(String problemId, String village, String domain, String description, Integer affectedPopulation, String severity, List<String> evidence, Instant timestamp, String source) {}

    /** Fact or evidence statement with strict category. */
    public record FactResponse(String factId, String statement, String source, String sourceType, String category, double confidence, Instant timestamp) {}

    /** Contributing factor that is not automatically treated as a root cause. */
    public record FactorResponse(String factor, List<String> supportingEvidence, List<String> contradictingEvidence, double confidence, String source) {}

    /** Candidate or validated root cause. */
    public record CandidateRootCauseResponse(String rootCauseId, String description, List<String> supportingFacts, List<String> supportingEvidence, List<String> contradictingEvidence, double confidence, String affectedDomain, List<String> assumptions, String uncertainty, String reasoningSummary) {}

    /** Alternative explanation compared against available evidence. */
    public record AlternativeHypothesisResponse(String hypothesisId, String description, List<String> supportingEvidence, List<String> missingEvidence, double confidence) {}

    /** Evidence scoring record. */
    public record EvidenceAssessmentResponse(String evidenceId, String statement, String source, String sourceType, double reliability, double relevance, double freshness, double consistency, double confidence, String category) {}

    /** Uncertainty and missing-evidence record. */
    public record UncertaintyResponse(String uncertaintyId, String statement, List<String> missingEvidence, List<String> followUpQuestions, String severity) {}

    /** Overall confidence record. */
    public record ConfidenceResponse(double overall, double sourceReliability, double evidenceRelevance, double evidenceFreshness, double evidenceQuantity, double evidenceConsistency, double contradictionPenalty, String interpretation) {}

    /** Causal graph edge with metadata. */
    public record CausalRelationshipResponse(String from, String to, String relationshipType, double confidence, List<String> evidence, String source) {}

    /** Human review response. */
    public record HumanReviewResponse(UUID reviewId, UUID analysisId, String action, String status, Instant reviewedAt) {}
}
