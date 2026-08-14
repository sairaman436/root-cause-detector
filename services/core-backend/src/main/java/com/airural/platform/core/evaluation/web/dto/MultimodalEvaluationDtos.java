/*
 * Purpose: Defines the server-backed multimodal trace and human-review contracts.
 * Why it exists: The portal must submit rubric data while the server owns trace membership and reviewer identity.
 * Architecture fit: REST DTO boundary for the multimodal evaluation workflow.
 */
package com.airural.platform.core.evaluation.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-backed multimodal evaluation records. */
public final class MultimodalEvaluationDtos {
    private MultimodalEvaluationDtos() {}

    /** Immutable trace exposed for review; the artifact has already been sanitized server-side. */
    public record TraceResponse(String traceId, String artifactVersion, String evaluationRound, String domain,
            String question, String imageName, String imageType, long imageSize, JsonNode artifact,
            String reviewStatus, int reviewCount) {}

    /** Queue and dashboard state for the authenticated reviewer. */
    public record TraceQueueResponse(String artifactVersion, String evaluationRound, String rubricVersion,
            int total, int scored, int remaining, int reviewerCount, List<TraceResponse> traces,
            List<DomainSummary> domainSummaries) {}

    /** Server-computed domain summary; averages are null when no score exists. */
    public record DomainSummary(String domain, int scored, Double observationAverage, Double evidenceAverage,
            Double rootCauseAverage, Double recommendationAverage, Double groundingAverage,
            Double usefulnessAverage, int recommendationSampleSize, Map<String, Integer> failureClassifications) {}

    /** Six multimodal rubric dimensions; recommendation may be intentionally not scored. */
    public record Scores(
            @NotNull @Min(1) @Max(5) Integer observationQuality,
            @NotNull @Min(1) @Max(5) Integer evidenceRelevance,
            @NotNull @Min(1) @Max(5) Integer rootCauseQuality,
            @Min(1) @Max(5) Integer recommendationQuality,
            @NotNull @Min(1) @Max(5) Integer grounding,
            @NotNull @Min(1) @Max(5) Integer overallUsefulness) {}

    /** Explicit unsupported-claim flags captured by the human reviewer. */
    public record UnsupportedClaimFlags(
            @NotNull Boolean observation,
            @NotNull Boolean evidence,
            @NotNull Boolean rootCause,
            @NotNull Boolean recommendation) {}

    /** Request body for one authenticated multimodal review. */
    public record ReviewRequest(
            @NotBlank @Size(max = 160) String traceId,
            @NotBlank @Size(max = 120) String artifactVersion,
            @NotBlank @Size(max = 120) String evaluationRound,
            @NotBlank @Size(max = 80) String rubricVersion,
            @NotNull @Valid Scores scores,
            @NotBlank @Size(max = 80) String failureClassification,
            @NotNull @Valid UnsupportedClaimFlags unsupportedClaimFlags,
            @Size(max = 4000) String reviewerComments) {}

    /** Persisted review response; reviewer identity is returned from the JWT-derived server value. */
    public record ReviewResponse(UUID reviewId, String traceId, String artifactVersion, String evaluationRound,
            String rubricVersion, UUID reviewerId, Instant reviewedAt, Scores scores,
            String failureClassification, UnsupportedClaimFlags unsupportedClaimFlags,
            String reviewerComments, String submissionStatus) {}
}

