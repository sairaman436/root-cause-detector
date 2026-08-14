/*
 * Purpose: Defines the authenticated human-evaluation REST contracts.
 * Why it exists: The portal needs stable read and submission shapes while the reviewer identity remains server-derived.
 * Architecture fit: REST DTO boundary for the evaluation bounded context.
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

/** Human evaluation API records. */
public final class HumanEvaluationDtos {
    private HumanEvaluationDtos() {}

    /** Immutable example exposed to an authorized reviewer. */
    public record HumanEvaluationExampleResponse(String evaluationSetVersion, String rubricVersion, String exampleId,
            String task, String scenarioGroup, String input, String retrievedContext, String output, JsonNode citations,
            JsonNode provenance, String modelVersion, String promptVersion, JsonNode inferenceConfiguration,
            String outputSha256, String reviewStatus, int reviewCount) {}

    /** Review queue response with explicit progress state. */
    public record HumanEvaluationExamplesResponse(String evaluationSetVersion, String rubricVersion, int total,
            int scored, int remaining, List<HumanEvaluationExampleResponse> examples) {}

    /** Request body for one independent human rubric submission. */
    public record HumanEvaluationReviewRequest(
            @NotBlank String exampleId,
            @NotNull @Valid Scores scores,
            @NotNull @Size(max = 50) List<@NotBlank @Size(max = 160) String> evidenceReferencesUsed,
            @Size(max = 4000) String reviewerComments) {}

    /** Rubric scores; task-specific required fields are enforced by the application service. */
    public record Scores(
            @Min(0) @Max(4) Integer rootCauseQuality,
            @Min(0) @Max(4) Integer recommendationQuality,
            @Min(0) @Max(4) Integer ragEvidenceQuality,
            @Min(0) @Max(4) Integer uncertaintyHandling,
            @Min(0) @Max(4) Integer practicalUsefulness) {}

    /** Persisted review response, including server-owned reviewer metadata. */
    public record HumanEvaluationReviewResponse(java.util.UUID reviewId, String evaluationSetVersion,
            String exampleId, String task, String rubricVersion, java.util.UUID reviewerId, Instant reviewedAt,
            Scores scores, List<String> evidenceReferencesUsed, String reviewerComments, String status) {}
}

