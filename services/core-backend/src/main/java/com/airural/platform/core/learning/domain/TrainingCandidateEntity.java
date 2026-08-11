/*
 * Purpose: Stores governed future-training candidate records.
 * Why it exists: No feedback sample may enter a future dataset without quality validation, privacy classification, lineage, and approval.
 * Architecture fit: Dataset candidate registry for future training cycles.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Training candidate entity. */
@Entity
@Table(name = "training_candidates", schema = "learning")
public class TrainingCandidateEntity {
    @Id private UUID id;
    private UUID learningRecordId;
    private String candidateDataset;
    private String source;
    private BigDecimal qualityScore;
    private String reviewer;
    private String datasetLineage;
    private String trainingReadiness;
    private String approvalStatus;
    private Instant createdAt;
    private UUID reviewerUserId;
    private String datasetVersion;
    private Boolean synthetic;
    private Instant reviewedAt;
    private String reviewDecision;
    private UUID reviewId;

    protected TrainingCandidateEntity() {}

    /** Creates a training candidate. */
    public TrainingCandidateEntity(UUID id, UUID learningRecordId, String candidateDataset, String source, BigDecimal qualityScore, String reviewer, String datasetLineage, String trainingReadiness, String approvalStatus, Instant createdAt) {
        this(id, learningRecordId, candidateDataset, source, qualityScore, reviewer, datasetLineage, trainingReadiness, approvalStatus, createdAt, null, null, source != null && source.toUpperCase().contains("SYNTHETIC"));
    }

    /** Creates a candidate with explicit reviewer and dataset lineage fields. */
    public TrainingCandidateEntity(UUID id, UUID learningRecordId, String candidateDataset, String source, BigDecimal qualityScore, String reviewer, String datasetLineage, String trainingReadiness, String approvalStatus, Instant createdAt, UUID reviewerUserId, String datasetVersion, Boolean synthetic) {
        this.id = id; this.learningRecordId = learningRecordId; this.candidateDataset = candidateDataset; this.source = source; this.qualityScore = qualityScore; this.reviewer = reviewer; this.datasetLineage = datasetLineage; this.trainingReadiness = trainingReadiness; this.approvalStatus = approvalStatus; this.createdAt = createdAt; this.reviewerUserId = reviewerUserId; this.datasetVersion = datasetVersion; this.synthetic = synthetic;
    }

    public UUID getId() { return id; }
    public UUID getLearningRecordId() { return learningRecordId; }
    public String getCandidateDataset() { return candidateDataset; }
    public String getSource() { return source; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public String getReviewer() { return reviewer; }
    public String getDatasetLineage() { return datasetLineage; }
    public String getTrainingReadiness() { return trainingReadiness; }
    public String getApprovalStatus() { return approvalStatus; }
    public UUID getReviewerUserId() { return reviewerUserId; }
    public String getDatasetVersion() { return datasetVersion; }
    public Boolean getSynthetic() { return synthetic; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getReviewDecision() { return reviewDecision; }

    /** Applies the final human review state. */
    public void review(String decision, String status, String readiness, UUID reviewerUserId, String datasetVersion) {
        review(decision, status, readiness, reviewerUserId, datasetVersion, null, Instant.now());
    }

    /** Applies the final human review state and preserves the review audit identity. */
    public void review(String decision, String status, String readiness, UUID reviewerUserId, String datasetVersion, UUID reviewId, Instant reviewedAt) {
        this.reviewDecision = decision;
        this.approvalStatus = status;
        this.trainingReadiness = readiness;
        this.reviewerUserId = reviewerUserId;
        this.datasetVersion = datasetVersion;
        this.reviewId = reviewId;
        this.reviewedAt = reviewedAt == null ? Instant.now() : reviewedAt;
    }

    public UUID getReviewId() { return reviewId; }
}
