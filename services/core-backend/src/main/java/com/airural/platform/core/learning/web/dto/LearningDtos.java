/*
 * Purpose: Defines REST contracts for continuous learning.
 * Why it exists: API clients need stable feedback, review, candidate, metric, promote, and reject payloads.
 * Architecture fit: DTO boundary for AI-7 REST APIs.
 */
package com.airural.platform.core.learning.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

/** Container for learning DTO records. */
public final class LearningDtos {
    private LearningDtos() {}

    /** Request to capture feedback and create a governed learning record. */
    public record FeedbackRequest(@NotBlank String sourceType, @NotBlank String modelVersion, @NotBlank String promptVersion, String retrievedContext, @NotBlank String input, @NotBlank String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, String feedbackSource, String feedbackType, String feedbackText, String privacyClassification) {}

    /** Request to review a learning record. */
    public record ReviewRequest(@NotNull UUID learningRecordId, @NotBlank String reviewer, @NotBlank String decision, String comments, Boolean createTrainingCandidate) {}

    /** Request to promote a candidate to future training dataset eligibility. */
    public record PromoteLearningRequest(@NotNull UUID trainingCandidateId, @NotBlank String reviewer, String rationale) {}

    /** Request to reject a candidate. */
    public record RejectLearningRequest(@NotNull UUID trainingCandidateId, @NotBlank String reviewer, String rationale) {}

    /** Learning record response. */
    public record LearningRecordResponse(UUID id, String sourceType, String modelVersion, Boolean trainingEligible, String privacyClassification, String approvalStatus) {}

    /** Training candidate response. */
    public record TrainingCandidateResponse(UUID id, String approvalStatus) {}

    /** Metrics response. */
    public record LearningMetricsResponse(Integer feedbackVolume, BigDecimal acceptanceRate, BigDecimal correctionRate, Integer trainingCandidateCount, String status) {}

    /** Decision response. */
    public record LearningDecisionResponse(UUID id, String decision, String status, String details) {}
}
