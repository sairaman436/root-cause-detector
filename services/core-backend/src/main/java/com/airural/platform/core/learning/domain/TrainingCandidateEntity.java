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

    protected TrainingCandidateEntity() {}

    /** Creates a training candidate. */
    public TrainingCandidateEntity(UUID id, UUID learningRecordId, String candidateDataset, String source, BigDecimal qualityScore, String reviewer, String datasetLineage, String trainingReadiness, String approvalStatus, Instant createdAt) {
        this.id = id; this.learningRecordId = learningRecordId; this.candidateDataset = candidateDataset; this.source = source; this.qualityScore = qualityScore; this.reviewer = reviewer; this.datasetLineage = datasetLineage; this.trainingReadiness = trainingReadiness; this.approvalStatus = approvalStatus; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getApprovalStatus() { return approvalStatus; }
}
