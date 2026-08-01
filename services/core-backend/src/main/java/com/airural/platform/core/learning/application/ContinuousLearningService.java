/*
 * Purpose: Coordinates feedback capture, review, quality validation, candidate registration, approvals, metrics, knowledge deltas, and audits.
 * Why it exists: AI-7 converts operational intelligence into governed future-training candidates without automatic retraining or deployment.
 * Architecture fit: Application service for the enterprise continuous learning platform.
 */
package com.airural.platform.core.learning.application;

import com.airural.platform.core.learning.domain.*;
import com.airural.platform.core.learning.infrastructure.*;
import com.airural.platform.core.learning.web.dto.LearningDtos.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for governed continuous learning. */
@Service
public class ContinuousLearningService {
    private final LearningRecordRepository records;
    private final FeedbackEventRepository feedbackEvents;
    private final CorrectionRepository corrections;
    private final HumanReviewRepository reviews;
    private final KnowledgeDeltaRepository knowledgeDeltas;
    private final TrainingCandidateRepository candidates;
    private final ApprovalWorkflowRepository approvals;
    private final LearningMetricsRepository metrics;
    private final LearningAuditRepository audits;

    public ContinuousLearningService(LearningRecordRepository records, FeedbackEventRepository feedbackEvents, CorrectionRepository corrections, HumanReviewRepository reviews, KnowledgeDeltaRepository knowledgeDeltas, TrainingCandidateRepository candidates, ApprovalWorkflowRepository approvals, LearningMetricsRepository metrics, LearningAuditRepository audits) {
        this.records = records; this.feedbackEvents = feedbackEvents; this.corrections = corrections; this.reviews = reviews; this.knowledgeDeltas = knowledgeDeltas; this.candidates = candidates; this.approvals = approvals; this.metrics = metrics; this.audits = audits;
    }

    /** Captures feedback and creates a governed learning record. */
    @Transactional
    public LearningRecordResponse feedback(FeedbackRequest request) {
        UUID recordId = UUID.randomUUID();
        String privacy = request.privacyClassification() == null ? "INTERNAL" : request.privacyClassification();
        LearningRecordEntity record = records.save(new LearningRecordEntity(recordId, Instant.now(), request.sourceType(), request.modelVersion(), request.promptVersion(), mask(request.retrievedContext()), mask(request.input()), mask(request.aiOutput()), mask(request.humanEditedOutput()), mask(request.acceptedOutput()), request.confidence() == null ? BigDecimal.valueOf(0.50) : request.confidence(), safeJson(request.evidenceUsedJson()), request.agentUsed(), request.reviewer(), false, privacy, "PENDING_HUMAN_REVIEW"));
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
        LearningRecordEntity record = findRecord(request.learningRecordId());
        reviews.save(new HumanReviewEntity(UUID.randomUUID(), record.getId(), request.reviewer(), request.decision(), escalation(request.decision()), safe(request.comments(), "not specified"), Instant.now()));
        audit(record.getId(), null, "HUMAN_REVIEW_" + request.decision().toUpperCase(), request.reviewer(), "{\"decision\":\"" + request.decision() + "\"}");
        if (Boolean.TRUE.equals(request.createTrainingCandidate()) && !"REJECT".equalsIgnoreCase(request.decision())) {
            TrainingCandidateEntity candidate = candidates.save(new TrainingCandidateEntity(UUID.randomUUID(), record.getId(), "future-rural-learning-dataset", record.getSourceType(), qualityScore(record), request.reviewer(), "learning-record:" + record.getId(), "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now()));
            approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "AI Governance Board", "PENDING", request.reviewer(), "Candidate queued after human review", Instant.now()));
            audit(record.getId(), candidate.getId(), "TRAINING_CANDIDATE_CREATED", request.reviewer(), "{\"readiness\":\"PENDING_DATASET_APPROVAL\"}");
            return new LearningDecisionResponse(candidate.getId(), "CANDIDATE_CREATED", "PENDING_APPROVAL", "Training candidate queued for governance; no model retraining was performed");
        }
        return new LearningDecisionResponse(record.getId(), request.decision(), "REVIEW_RECORDED", "Human review recorded without creating training data");
    }

    /** Lists training candidates. */
    @Transactional(readOnly = true)
    public Page<TrainingCandidateResponse> candidates(Pageable pageable) {
        return candidates.findAll(pageable).map(candidate -> new TrainingCandidateResponse(candidate.getId(), candidate.getApprovalStatus()));
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

    /** Rejects a training candidate. */
    @Transactional
    public LearningDecisionResponse reject(RejectLearningRequest request) {
        TrainingCandidateEntity candidate = findCandidate(request.trainingCandidateId());
        approvals.save(new ApprovalWorkflowEntity(UUID.randomUUID(), candidate.getId(), "Data Governance Board", "REJECTED", request.reviewer(), safe(request.rationale(), "rejected"), Instant.now()));
        audit(null, candidate.getId(), "CANDIDATE_REJECTED", request.reviewer(), "{\"futureDatasetOnly\":false}");
        return new LearningDecisionResponse(candidate.getId(), "REJECT", "REJECTED", "Candidate rejected and excluded from future training datasets");
    }

    private LearningRecordEntity findRecord(UUID id) {
        return records.findById(id).orElseThrow(() -> new LearningException(HttpStatus.NOT_FOUND, "LEARNING_RECORD_NOT_FOUND", "Learning record was not found"));
    }

    private TrainingCandidateEntity findCandidate(UUID id) {
        return candidates.findById(id).orElseThrow(() -> new LearningException(HttpStatus.NOT_FOUND, "TRAINING_CANDIDATE_NOT_FOUND", "Training candidate was not found"));
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

    private String mask(String value) {
        if (value == null) return null;
        return value.replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[EMAIL_MASKED]").replaceAll("\\b\\d{10}\\b", "[PHONE_MASKED]").replace("\"", "'");
    }

    private String safeJson(String value) {
        return value == null || value.isBlank() ? "{}" : mask(value);
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
