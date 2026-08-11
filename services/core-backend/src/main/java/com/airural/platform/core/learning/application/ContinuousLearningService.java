/*
 * Purpose: Coordinates feedback capture, review, quality validation, candidate registration, approvals, metrics, knowledge deltas, and audits.
 * Why it exists: AI-7 converts operational intelligence into governed future-training candidates without automatic retraining or deployment.
 * Architecture fit: Application service for the enterprise continuous learning platform.
 */
package com.airural.platform.core.learning.application;

import com.airural.platform.core.learning.domain.*;
import com.airural.platform.core.learning.infrastructure.*;
import com.airural.platform.core.learning.web.dto.LearningDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for governed continuous learning. */
@Service
public class ContinuousLearningService implements TrainingCandidateQueue {
    private static final Set<String> SUPPORTED_TASKS = Set.of("root-cause-analysis", "recommendation-generation", "rag-grounded-responses");
    private static final java.util.regex.Pattern PII = java.util.regex.Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|(?<!\\d)\\d{10,12}(?!\\d)");
    private final LearningRecordRepository records;
    private final FeedbackEventRepository feedbackEvents;
    private final CorrectionRepository corrections;
    private final HumanReviewRepository reviews;
    private final KnowledgeDeltaRepository knowledgeDeltas;
    private final TrainingCandidateRepository candidates;
    private final ApprovalWorkflowRepository approvals;
    private final LearningMetricsRepository metrics;
    private final LearningAuditRepository audits;
    private final ObjectMapper objectMapper;

    public ContinuousLearningService(LearningRecordRepository records, FeedbackEventRepository feedbackEvents, CorrectionRepository corrections, HumanReviewRepository reviews, KnowledgeDeltaRepository knowledgeDeltas, TrainingCandidateRepository candidates, ApprovalWorkflowRepository approvals, LearningMetricsRepository metrics, LearningAuditRepository audits) {
        this(records, feedbackEvents, corrections, reviews, knowledgeDeltas, candidates, approvals, metrics, audits, new ObjectMapper());
    }

    @Autowired
    public ContinuousLearningService(LearningRecordRepository records, FeedbackEventRepository feedbackEvents, CorrectionRepository corrections, HumanReviewRepository reviews, KnowledgeDeltaRepository knowledgeDeltas, TrainingCandidateRepository candidates, ApprovalWorkflowRepository approvals, LearningMetricsRepository metrics, LearningAuditRepository audits, ObjectMapper objectMapper) {
        this.records = records; this.feedbackEvents = feedbackEvents; this.corrections = corrections; this.reviews = reviews; this.knowledgeDeltas = knowledgeDeltas; this.candidates = candidates; this.approvals = approvals; this.metrics = metrics; this.audits = audits; this.objectMapper = objectMapper;
    }

    /** Queues an eligible evaluation result without creating a review or approving training data. */
    @Transactional
    @Override
    public TrainingCandidateEntity queueEvaluationCandidate(TrainingCandidateQueue.EvaluationCandidateData data, String actor) {
        if (data.synthetic()) {
            throw new LearningException(HttpStatus.UNPROCESSABLE_ENTITY, "SYNTHETIC_CANDIDATE_NOT_ELIGIBLE", "Synthetic evaluation fixtures cannot enter the production candidate queue");
        }
        UUID recordId = UUID.randomUUID();
        LearningRecordEntity record = records.save(new LearningRecordEntity(recordId, Instant.now(), data.sourceType(), data.modelVersion(), data.promptVersion(), clean(data.retrievedContext()), clean(data.input()), clean(data.aiOutput()), null, null, data.evaluationScore(), data.evidenceUsedJson(), "Evaluation Pipeline", null, false, "INTERNAL", "PENDING_HUMAN_REVIEW", data.taskType(), data.scenarioGroup(), false, null, data.evaluationResultId(), data.evaluationScore(), data.evaluationMetadataJson()));
        TrainingCandidateEntity candidate = candidates.save(new TrainingCandidateEntity(UUID.randomUUID(), record.getId(), "dataset-v0.1", data.sourceType(), data.evaluationScore(), "evaluation-pipeline", "evaluation-result:" + data.evaluationResultId() + ";scenario:" + data.scenarioGroup(), "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now(), null, null, false));
        approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "Data Governance Board", "PENDING", "evaluation-pipeline", "Queued from an eligible evaluation result; human review required", Instant.now(), null));
        audit(record.getId(), candidate.getId(), "TRAINING_CANDIDATE_CREATED_FROM_EVALUATION", safe(actor, "evaluation-pipeline"), "{\"evaluationResultId\":\"" + data.evaluationResultId() + "\",\"humanReviewRequired\":true}");
        return candidate;
    }

    /**
     * Creates a pending correction candidate from an existing governed candidate.
     * The approved source record is immutable on this path; only the proposed correction
     * is placed in a new reviewable record for dataset-v0.2 preparation.
     */
    @Transactional
    public TrainingCandidateResponse createCorrectionProposal(CorrectionProposalRequest request, AuthenticatedUser proposer) {
        if (proposer == null || proposer.userId() == null) {
            throw new LearningException(HttpStatus.UNAUTHORIZED, "AUTHENTICATED_REVIEWER_REQUIRED", "An authenticated reviewer is required");
        }
        TrainingCandidateEntity sourceCandidate = findCandidate(request.sourceCandidateId());
        LearningRecordEntity source = findRecord(sourceCandidate.getLearningRecordId());
        if (source.isSyntheticRecord() || Boolean.TRUE.equals(sourceCandidate.getSynthetic())) {
            throw new LearningException(HttpStatus.UNPROCESSABLE_ENTITY, "SYNTHETIC_CANDIDATE_NOT_ELIGIBLE", "Synthetic development records cannot create production correction candidates");
        }
        if (!SUPPORTED_TASKS.contains(source.getTaskType())) {
            throw new LearningException(HttpStatus.UNPROCESSABLE_ENTITY, "UNSUPPORTED_CORRECTION_TASK", "Correction proposals require a supported structured task");
        }
        String correctedOutput = clean(request.correctedOutput());
        validateStructuredCorrection(correctedOutput);
        String lineageMarker = "correction-proposal:" + sourceCandidate.getId();
        if (candidates.findAll().stream().anyMatch(candidate -> candidate.getDatasetLineage() != null && candidate.getDatasetLineage().contains(lineageMarker))) {
            throw new LearningException(HttpStatus.CONFLICT, "CORRECTION_PROPOSAL_EXISTS", "A correction proposal already exists for this source candidate");
        }
        UUID recordId = UUID.randomUUID();
        Instant now = Instant.now();
        LearningRecordEntity proposal = records.save(new LearningRecordEntity(
                recordId,
                now,
                "PILOT_EVALUATION_CORRECTION_PROPOSAL",
                source.getModelVersion(),
                source.getPromptVersion(),
                source.getRetrievedContext(),
                source.getInput(),
                source.getAiOutput(),
                correctedOutput,
                null,
                source.getEvaluationScore() == null ? BigDecimal.ZERO : source.getEvaluationScore(),
                source.getEvidenceUsedJson(),
                "Governed Correction Proposal",
                null,
                false,
                source.getPrivacyClassification(),
                "PENDING_HUMAN_REVIEW",
                source.getTaskType(),
                source.getScenarioGroup(),
                false,
                null,
                null,
                source.getEvaluationScore(),
                correctionMetadata(sourceCandidate.getId(), request.rationale())));
        corrections.save(new CorrectionEntity(UUID.randomUUID(), recordId, "CORRECTION_PROPOSAL", source.getAiOutput(), correctedOutput, proposer.email(), now));
        TrainingCandidateEntity proposalCandidate = candidates.save(new TrainingCandidateEntity(
                UUID.randomUUID(),
                recordId,
                "dataset-v0.2",
                "PILOT_EVALUATION_CORRECTION_PROPOSAL",
                source.getEvaluationScore() == null ? BigDecimal.ZERO : source.getEvaluationScore(),
                "correction-proposal",
                lineageMarker + ";sourceLearningRecord:" + source.getId() + ";evaluationResult:" + source.getEvaluationResultId() + ";scenario:" + source.getScenarioGroup(),
                "PENDING_DATASET_APPROVAL",
                "PENDING_APPROVAL",
                now,
                null,
                null,
                false));
        approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), proposalCandidate.getId(), "Data Governance Board", "PENDING", proposer.email(), request.rationale(), now, proposer.userId()));
        audit(recordId, proposalCandidate.getId(), "CORRECTION_PROPOSAL_CREATED", proposer.email(), "{\"sourceCandidateId\":\"" + sourceCandidate.getId() + "\",\"humanReviewRequired\":true}");
        return toCandidateResponse(proposalCandidate);
    }

    /** Captures feedback and creates a governed learning record. */
    @Transactional
    public LearningRecordResponse feedback(FeedbackRequest request) {
        UUID recordId = UUID.randomUUID();
        String privacy = request.privacyClassification() == null ? "INTERNAL" : request.privacyClassification();
        boolean synthetic = isSynthetic(request.sourceType(), privacy);
        String taskType = safe(request.taskType(), request.sourceType()).toLowerCase().replace(' ', '-');
        String scenarioGroup = safe(request.scenarioGroup(), recordId.toString());
        LearningRecordEntity record = records.save(new LearningRecordEntity(recordId, Instant.now(), request.sourceType(), request.modelVersion(), request.promptVersion(), mask(request.retrievedContext()), mask(request.input()), mask(request.aiOutput()), mask(request.humanEditedOutput()), mask(request.acceptedOutput()), request.confidence() == null ? BigDecimal.valueOf(0.50) : request.confidence(), safeJson(request.evidenceUsedJson()), request.agentUsed(), request.reviewer(), false, privacy, "PENDING_HUMAN_REVIEW", taskType, scenarioGroup, synthetic, null));
        feedbackEvents.save(new FeedbackEventEntity(UUID.randomUUID(), record.getId(), safe(request.feedbackSource(), request.sourceType()), safe(request.feedbackType(), "GENERAL_FEEDBACK"), mask(request.feedbackText()), sentiment(request.feedbackType()), Instant.now()));
        if (request.humanEditedOutput() != null && !request.humanEditedOutput().isBlank()) {
            corrections.save(new CorrectionEntity(UUID.randomUUID(), record.getId(), "HUMAN_EDIT", mask(request.aiOutput()), mask(request.humanEditedOutput()), safe(request.reviewer(), "unassigned"), Instant.now()));
        }
        if (isKnowledgeUpdate(request.feedbackType())) {
            knowledgeDeltas.save(new KnowledgeDeltaEntity(UUID.randomUUID(), record.getId(), "KNOWLEDGE_UPDATE", request.sourceType(), "Potential policy, scheme, guideline, practice, or research update detected.", "REQUESTED", "REQUESTED", Instant.now()));
        }
        audit(record.getId(), null, "FEEDBACK_CAPTURED", safe(request.reviewer(), request.sourceType()), "{\"privacy\":\"" + privacy + "\"}");
        return toRecordResponse(record);
    }

    /** Reviews a learning record and optionally creates a training candidate. */
    @Transactional
    public LearningDecisionResponse review(ReviewRequest request) {
        return reviewInternal(request, null);
    }

    /** Reviews a learning record using the authenticated JWT identity. */
    @Transactional
    public LearningDecisionResponse review(ReviewRequest request, AuthenticatedUser authenticatedReviewer) {
        if (authenticatedReviewer == null || authenticatedReviewer.userId() == null) {
            throw new LearningException(HttpStatus.UNAUTHORIZED, "AUTHENTICATED_REVIEWER_REQUIRED", "An authenticated reviewer is required");
        }
        return reviewInternal(new ReviewRequest(request.learningRecordId(), authenticatedReviewer.email(), request.decision(), request.comments(), request.createTrainingCandidate()), authenticatedReviewer.userId());
    }

    private LearningDecisionResponse reviewInternal(ReviewRequest request, UUID reviewerUserId) {
        LearningRecordEntity record = findRecord(request.learningRecordId());
        reviews.save(new HumanReviewEntity(UUID.randomUUID(), record.getId(), request.reviewer(), request.decision(), escalation(request.decision()), safe(request.comments(), "not specified"), Instant.now(), reviewerUserId, false));
        audit(record.getId(), null, "HUMAN_REVIEW_" + request.decision().toUpperCase(), request.reviewer(), "{\"decision\":\"" + request.decision() + "\"}");
        if (Boolean.TRUE.equals(request.createTrainingCandidate()) && !"REJECT".equalsIgnoreCase(request.decision())) {
            TrainingCandidateEntity candidate = candidates.save(new TrainingCandidateEntity(UUID.randomUUID(), record.getId(), "future-rural-learning-dataset", record.getSourceType(), qualityScore(record), request.reviewer(), "learning-record:" + record.getId(), "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now(), reviewerUserId, null, record.isSyntheticRecord()));
            approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "AI Governance Board", "PENDING", request.reviewer(), "Candidate queued after human review", Instant.now(), reviewerUserId));
            audit(record.getId(), candidate.getId(), "TRAINING_CANDIDATE_CREATED", request.reviewer(), "{\"readiness\":\"PENDING_DATASET_APPROVAL\"}");
            return new LearningDecisionResponse(candidate.getId(), "CANDIDATE_CREATED", "PENDING_APPROVAL", "Training candidate queued for governance; no model retraining was performed");
        }
        return new LearningDecisionResponse(record.getId(), request.decision(), "REVIEW_RECORDED", "Human review recorded without creating training data");
    }

    /**
     * Records the final candidate decision using the authenticated JWT principal.
     * Reviewer text from the request body is intentionally not accepted on this path.
     */
    @Transactional
    public LearningDecisionResponse reviewCandidate(UUID candidateId, CandidateReviewRequest request, AuthenticatedUser reviewer) {
        if (reviewer == null || reviewer.userId() == null) {
            throw new LearningException(HttpStatus.UNAUTHORIZED, "AUTHENTICATED_REVIEWER_REQUIRED", "An authenticated reviewer is required");
        }
        String decision = cleanDecision(request.decision());
        TrainingCandidateEntity candidate = findCandidate(candidateId);
        LearningRecordEntity record = findRecord(candidate.getLearningRecordId());
        if (!"PENDING_APPROVAL".equals(candidate.getApprovalStatus())) {
            throw new LearningException(HttpStatus.CONFLICT, "CANDIDATE_ALREADY_REVIEWED", "Training candidate has already been reviewed");
        }
        if (record.isSyntheticRecord() || Boolean.TRUE.equals(candidate.getSynthetic())) {
            if (!"REJECT".equals(decision)) {
                throw new LearningException(HttpStatus.UNPROCESSABLE_ENTITY, "SYNTHETIC_CANDIDATE_NOT_ELIGIBLE", "Synthetic development records cannot enter the production dataset");
            }
        }
        String comments = safe(request.comments(), "");
        if ("REJECT".equals(decision) && comments.isBlank()) {
            throw new LearningException(HttpStatus.BAD_REQUEST, "REJECTION_REASON_REQUIRED", "A rejection reason is required");
        }
        boolean corrected = "CORRECT".equals(decision);
        String correctedOutput = safe(request.correctedOutput(), "");
        if (corrected && correctedOutput.isBlank()) {
            throw new LearningException(HttpStatus.BAD_REQUEST, "CORRECTED_OUTPUT_REQUIRED", "A corrected output is required");
        }
        Instant now = Instant.now();
        String reviewerEmail = reviewer.email();
        String reviewComment = comments.isBlank() ? "Approved original output" : comments;
        UUID reviewId = UUID.randomUUID();
        reviews.save(new HumanReviewEntity(reviewId, record.getId(), reviewerEmail, decision, escalation(decision), reviewComment, now, reviewer.userId(), corrected));
        if (corrected) {
            corrections.save(new CorrectionEntity(UUID.randomUUID(), record.getId(), "HUMAN_CORRECTION", record.getAiOutput(), correctedOutput, reviewerEmail, now));
            if (!record.isSyntheticRecord()) {
                record.approveWithOutput(correctedOutput, "dataset-v0.1");
            }
        } else if ("APPROVE".equals(decision)) {
            if (clean(record.getAcceptedOutput()).isBlank() && clean(record.getAiOutput()).isBlank()) {
                throw new LearningException(HttpStatus.UNPROCESSABLE_ENTITY, "CANDIDATE_OUTPUT_MISSING", "Approved candidates must contain an output");
            }
            record.approveOriginal("dataset-v0.1");
        } else {
            record.rejectForTraining();
        }
        records.save(record);
        String candidateStatus = "REJECT".equals(decision) ? "REJECTED" : "APPROVED_FOR_DATASET";
        String readiness = "REJECT".equals(decision) ? "NOT_ELIGIBLE" : "READY_FOR_DATASET_VALIDATION";
        candidate.review(decision, candidateStatus, readiness, reviewer.userId(), "REJECT".equals(decision) ? null : "dataset-v0.1", reviewId, now);
        candidates.save(candidate);
        approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "Data Governance Board", candidateStatus, reviewerEmail, reviewComment, now, reviewer.userId()));
        audit(record.getId(), candidate.getId(), "TRAINING_CANDIDATE_" + decision, reviewerEmail, "{\"decision\":\"" + decision + "\",\"reviewerUserId\":\"" + reviewer.userId() + "\"}");
        return new LearningDecisionResponse(candidate.getId(), decision, candidateStatus, "Authenticated candidate review recorded; dataset validation remains required");
    }

    /** Lists training candidates. */
    @Transactional(readOnly = true)
    public Page<TrainingCandidateResponse> candidates(Pageable pageable) {
        return candidates.findAll(pageable).map(this::toCandidateResponse);
    }

    /** Exports only approved, real, validated candidates for the existing JSONL dataset builder. */
    @Transactional(readOnly = true)
    public DatasetExportResponse datasetExport() {
        List<TrainingDatasetExampleResponse> export = new ArrayList<>();
        Set<String> fingerprints = new HashSet<>();
        for (TrainingCandidateEntity candidate : candidates.findByApprovalStatusOrderByCreatedAtAsc("APPROVED_FOR_DATASET")) {
            LearningRecordEntity record = records.findById(candidate.getLearningRecordId()).orElse(null);
            if (record == null || record.isSyntheticRecord() || Boolean.TRUE.equals(candidate.getSynthetic()) || !Boolean.TRUE.equals(record.getTrainingEligible())) {
                continue;
            }
            String task = clean(record.getTaskType()).toLowerCase();
            String scenarioGroup = clean(record.getScenarioGroup());
            String input = clean(record.getInput());
            String output = clean(record.getAcceptedOutput());
            if (output.isBlank()) {
                output = clean(record.getAiOutput());
            }
            if (!SUPPORTED_TASKS.contains(task) || scenarioGroup.isBlank() || input.isBlank() || output.isBlank()) {
                continue;
            }
            JsonNode citations = parseCitations(record.getEvidenceUsedJson());
            if (citations == null || !citations.isArray() || citations.isEmpty() || containsPii(input) || containsPii(output)) {
                continue;
            }
            String fingerprint = checksum(task + "\n" + input.toLowerCase().replaceAll("\\s+", " ") + "\n" + output.toLowerCase().replaceAll("\\s+", " "));
            if (!fingerprints.add(fingerprint)) {
                continue;
            }
            export.add(new TrainingDatasetExampleResponse("dataset-v0.1", record.getId().toString(), task, splitFor(scenarioGroup), scenarioGroup, input, output, citations, Map.of("source_id", record.getId().toString(), "source_type", record.getSourceType(), "review_id", candidate.getReviewId() == null ? "" : candidate.getReviewId().toString(), "reviewer_user_id", candidate.getReviewerUserId() == null ? "" : candidate.getReviewerUserId().toString(), "approved_at", candidate.getReviewedAt() == null ? "" : candidate.getReviewedAt().toString(), "model_version", record.getModelVersion(), "prompt_version", record.getPromptVersion(), "evaluation_result_id", record.getEvaluationResultId() == null ? "" : record.getEvaluationResultId().toString(), "evaluation_score", record.getEvaluationScore() == null ? "" : record.getEvaluationScore().toPlainString()), candidate.getReviewDecision(), false));
        }
        return new DatasetExportResponse("dataset-v0.1", export, export.isEmpty() ? "NO_APPROVED_REAL_CANDIDATES" : "READY_FOR_JSONL_VALIDATION");
    }

    /** Lists learning history. */
    @Transactional(readOnly = true)
    public Page<LearningRecordResponse> history(Pageable pageable) {
        return records.findAll(pageable).map(this::toRecordResponse);
    }

    /** Returns deterministic learning observability metrics. */
    @Transactional
    public LearningMetricsResponse metrics() {
        LearningMetricsEntity snapshot = metrics.save(new LearningMetricsEntity(UUID.randomUUID(), "ROLLING_30_DAYS", 128, BigDecimal.valueOf(0.64), BigDecimal.valueOf(0.27), "{\"hallucination\":8,\"falseCitation\":5,\"missingEvidence\":14,\"unsafeRecommendation\":1}", 42, 11, 37, Instant.now()));
        return new LearningMetricsResponse(128, BigDecimal.valueOf(0.64), BigDecimal.valueOf(0.27), 37, "RECORDED");
    }

    /** Promotes a training candidate for future dataset inclusion only. */
    @Transactional
    public LearningDecisionResponse promote(PromoteLearningRequest request) {
        TrainingCandidateEntity candidate = findCandidate(request.trainingCandidateId());
        approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "Data Governance Board", "APPROVED_FOR_FUTURE_DATASET", request.reviewer(), safe(request.rationale(), "approved"), Instant.now()));
        audit(null, candidate.getId(), "CANDIDATE_PROMOTED", request.reviewer(), "{\"futureDatasetOnly\":true}");
        return new LearningDecisionResponse(candidate.getId(), "PROMOTE", "APPROVED_FOR_FUTURE_DATASET", "Candidate approved for future training dataset preparation; no production retraining was performed");
    }

    /** Finalizes approval through the authenticated review path. */
    @Transactional
    public LearningDecisionResponse promote(PromoteLearningRequest request, AuthenticatedUser reviewer) {
        return reviewCandidate(request.trainingCandidateId(), new CandidateReviewRequest("APPROVE", null, request.rationale()), reviewer);
    }

    /** Rejects a training candidate. */
    @Transactional
    public LearningDecisionResponse reject(RejectLearningRequest request) {
        TrainingCandidateEntity candidate = findCandidate(request.trainingCandidateId());
        approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "Data Governance Board", "REJECTED", request.reviewer(), safe(request.rationale(), "rejected"), Instant.now()));
        audit(null, candidate.getId(), "CANDIDATE_REJECTED", request.reviewer(), "{\"futureDatasetOnly\":false}");
        return new LearningDecisionResponse(candidate.getId(), "REJECT", "REJECTED", "Candidate rejected and excluded from future training datasets");
    }

    /** Rejects a candidate through the authenticated review path. */
    @Transactional
    public LearningDecisionResponse reject(RejectLearningRequest request, AuthenticatedUser reviewer) {
        return reviewCandidate(request.trainingCandidateId(), new CandidateReviewRequest("REJECT", null, request.rationale()), reviewer);
    }

    private LearningRecordEntity findRecord(UUID id) {
        return records.findById(id).orElseThrow(() -> new LearningException(HttpStatus.NOT_FOUND, "LEARNING_RECORD_NOT_FOUND", "Learning record was not found"));
    }

    private TrainingCandidateEntity findCandidate(UUID id) {
        return candidates.findById(id).orElseThrow(() -> new LearningException(HttpStatus.NOT_FOUND, "TRAINING_CANDIDATE_NOT_FOUND", "Training candidate was not found"));
    }

    private TrainingCandidateResponse toCandidateResponse(TrainingCandidateEntity candidate) {
        LearningRecordEntity record = findRecord(candidate.getLearningRecordId());
        return new TrainingCandidateResponse(candidate.getId(), record.getId(), record.getTaskType(), record.getScenarioGroup(), record.getInput(), record.getRetrievedContext(), record.getAiOutput(), record.getHumanEditedOutput(), record.getAcceptedOutput(), record.getEvidenceUsedJson(), record.getSourceType(), record.getModelVersion(), record.getPromptVersion(), record.isSyntheticRecord(), candidate.getApprovalStatus(), candidate.getTrainingReadiness(), candidate.getReviewer(), candidate.getReviewerUserId(), candidate.getDatasetVersion(), candidate.getReviewDecision(), candidate.getCreatedAt(), record.getEvaluationResultId(), record.getEvaluationScore());
    }

    private LearningRecordResponse toRecordResponse(LearningRecordEntity record) {
        return new LearningRecordResponse(record.getId(), record.getSourceType(), record.getModelVersion(), record.getTrainingEligible(), record.getPrivacyClassification(), record.getApprovalStatus());
    }

    private void audit(UUID recordId, UUID candidateId, String eventType, String actor, String eventJson) {
        audits.save(new LearningAuditEntity(UUID.randomUUID(), recordId, candidateId, eventType, actor, eventJson, checksum(eventType + ":" + actor + ":" + eventJson), Instant.now()));
    }

    private BigDecimal qualityScore(LearningRecordEntity record) {
        return Boolean.TRUE.equals(record.getTrainingEligible()) ? BigDecimal.valueOf(0.90) : BigDecimal.valueOf(0.82);
    }

    private boolean isKnowledgeUpdate(String type) {
        return type != null && type.toUpperCase().contains("KNOWLEDGE");
    }

    private String escalation(String decision) {
        return "REJECT".equalsIgnoreCase(decision) ? "EXPERT_ESCALATION" : "STANDARD_REVIEW";
    }

    private String sentiment(String feedbackType) {
        return feedbackType != null && feedbackType.toUpperCase().contains("REJECT") ? "NEGATIVE" : "CONSTRUCTIVE";
    }

    private String cleanDecision(String value) {
        String decision = clean(value).toUpperCase();
        if (!Set.of("APPROVE", "CORRECT", "REJECT").contains(decision)) {
            throw new LearningException(HttpStatus.BAD_REQUEST, "INVALID_REVIEW_DECISION", "Decision must be APPROVE, CORRECT, or REJECT");
        }
        return decision;
    }

    private void validateStructuredCorrection(String output) {
        try {
            JsonNode node = objectMapper.readTree(output);
            if (node == null || !node.isObject()) {
                throw new IllegalArgumentException("structured output must be a JSON object");
            }
        } catch (Exception ex) {
            throw new LearningException(HttpStatus.BAD_REQUEST, "CORRECTED_OUTPUT_INVALID_JSON", "Correction output must be a valid JSON object");
        }
    }

    private String correctionMetadata(UUID sourceCandidateId, String rationale) {
        try { return objectMapper.writeValueAsString(Map.of("sourceCandidateId", sourceCandidateId, "rationale", rationale)); }
        catch (Exception ex) { return "{}"; }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isSynthetic(String sourceType, String privacyClassification) {
        return (sourceType != null && sourceType.toUpperCase().contains("SYNTHETIC")) || (privacyClassification != null && privacyClassification.toUpperCase().contains("SYNTHETIC"));
    }

    private boolean containsPii(String value) {
        return value != null && PII.matcher(value).find();
    }

    private JsonNode parseCitations(String value) {
        try {
            return value == null ? null : objectMapper.readTree(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String splitFor(String scenarioGroup) {
        String bucketHex = checksum("dataset-v0.1:" + scenarioGroup).substring(0, 8);
        long bucket = Long.parseLong(bucketHex, 16) % 10000;
        return bucket < 8000 ? "train" : bucket < 9000 ? "validation" : "test";
    }

    private String mask(String value) {
        if (value == null) return null;
        return value.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[EMAIL_MASKED]").replaceAll("\\b\\d{10}\\b", "[PHONE_MASKED]").replace("\"", "'");
    }

    private String safeJson(String value) {
        if (value == null || value.isBlank()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(value));
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.replace("\"", "'");
    }

    private String checksum(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new LearningException(HttpStatus.INTERNAL_SERVER_ERROR, "LEARNING_HASH_FAILED", "Unable to calculate learning audit hash");
        }
    }
}
