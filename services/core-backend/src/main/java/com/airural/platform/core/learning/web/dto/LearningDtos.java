/*
 * Purpose: Defines REST contracts for continuous learning.
 * Why it exists: API clients need stable feedback, review, candidate, metric, promote, and reject payloads.
 * Architecture fit: DTO boundary for AI-7 REST APIs.
 */
package com.airural.platform.core.learning.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Container for learning DTO records. */
public final class LearningDtos {
    private LearningDtos() {}

    /** Request to capture feedback and create a governed learning record. */
    public record FeedbackRequest(@NotBlank String sourceType, @NotBlank String modelVersion, @NotBlank String promptVersion, String retrievedContext, @NotBlank String input, @NotBlank String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, String feedbackSource, String feedbackType, String feedbackText, String privacyClassification, String taskType, String scenarioGroup) {
        /** Backward-compatible constructor for existing feedback clients. */
        public FeedbackRequest(String sourceType, String modelVersion, String promptVersion, String retrievedContext, String input, String aiOutput, String humanEditedOutput, String acceptedOutput, BigDecimal confidence, String evidenceUsedJson, String agentUsed, String reviewer, String feedbackSource, String feedbackType, String feedbackText, String privacyClassification) {
            this(sourceType, modelVersion, promptVersion, retrievedContext, input, aiOutput, humanEditedOutput, acceptedOutput, confidence, evidenceUsedJson, agentUsed, reviewer, feedbackSource, feedbackType, feedbackText, privacyClassification, null, null);
        }
    }

    /** Request to review a learning record. */
    public record ReviewRequest(@NotNull UUID learningRecordId, @NotBlank String reviewer, @NotBlank String decision, String comments, Boolean createTrainingCandidate) {}

    /** Request for the authenticated reviewer to decide a training candidate. */
    public record CandidateReviewRequest(@NotBlank String decision, String correctedOutput, String comments) {}

    /** Request to queue eligible per-scenario evaluation results for human review. */
    public record CandidateGenerationRequest(UUID pilotRunId) {}

    /** Request to place a proposed source-data correction into the authenticated review queue. */
    public record CorrectionProposalRequest(@NotNull UUID sourceCandidateId, @NotBlank String correctedOutput, @NotBlank String rationale) {}

    /** Outcome of an evaluation-to-candidate generation pass. */
    public record CandidateGenerationResponse(int candidatesGenerated, int candidatesBlocked, int duplicatesSkipped, Map<String, Integer> blockedReasons) {}

    /** Request to promote a candidate to future training dataset eligibility. */
    public record PromoteLearningRequest(@NotNull UUID trainingCandidateId, @NotBlank String reviewer, String rationale) {}

    /** Request to reject a candidate. */
    public record RejectLearningRequest(@NotNull UUID trainingCandidateId, @NotBlank String reviewer, String rationale) {}

    /** Learning record response. */
    public record LearningRecordResponse(UUID id, String sourceType, String modelVersion, Boolean trainingEligible, String privacyClassification, String approvalStatus) {}

    /** Training candidate response with the reviewable source record. */
    public record TrainingCandidateResponse(UUID id, UUID learningRecordId, String taskType, String scenarioGroup, String input, String retrievedContext, String aiOutput, String humanEditedOutput, String acceptedOutput, String evidenceUsedJson, String sourceType, String modelVersion, String promptVersion, Boolean synthetic, String approvalStatus, String trainingReadiness, String reviewer, UUID reviewerUserId, String datasetVersion, String reviewDecision, Instant createdAt, UUID evaluationResultId, BigDecimal evaluationScore) {}

    /** Dataset export record consumed by the existing JSONL validator. */
    public record TrainingDatasetExampleResponse(String datasetVersion, String exampleId, String task, String split, String scenarioGroup, String input, String output, JsonNode citations, Map<String, Object> provenance, String reviewDecision, Boolean synthetic) {}

    /** Governed dataset export response. */
    public record DatasetExportResponse(String datasetVersion, List<TrainingDatasetExampleResponse> examples, String status) {}

    /** Metrics response. */
    public record LearningMetricsResponse(Integer feedbackVolume, BigDecimal acceptanceRate, BigDecimal correctionRate, Integer trainingCandidateCount, String status) {}

    /** Decision response. */
    public record LearningDecisionResponse(UUID id, String decision, String status, String details) {}
}
