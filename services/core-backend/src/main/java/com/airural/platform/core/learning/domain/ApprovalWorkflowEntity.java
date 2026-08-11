/*
 * Purpose: Stores governance board approval decisions for learning candidates.
 * Why it exists: AI Governance, Data Governance, Security, Architecture, and Release review must approve eligible learning data.
 * Architecture fit: Approval workflow entity for dataset candidate governance.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Approval workflow entity. */
@Entity
@Table(name = "approval_workflows", schema = "learning")
public class ApprovalWorkflowEntity {
    @Id private UUID id;
    private UUID trainingCandidateId;
    private String reviewBoard;
    private String decision;
    private String reviewer;
    @Column(columnDefinition = "TEXT") private String rationale;
    private Instant decidedAt;
    private UUID reviewerUserId;

    protected ApprovalWorkflowEntity() {}

    /** Creates an approval workflow record. */
    public ApprovalWorkflowEntity(UUID id, UUID trainingCandidateId, String reviewBoard, String decision, String reviewer, String rationale, Instant decidedAt) {
        this(id, trainingCandidateId, reviewBoard, decision, reviewer, rationale, decidedAt, null);
    }

    /** Creates an approval record with authenticated reviewer identity. */
    public ApprovalWorkflowEntity(UUID id, UUID trainingCandidateId, String reviewBoard, String decision, String reviewer, String rationale, Instant decidedAt, UUID reviewerUserId) {
        this.id = id; this.trainingCandidateId = trainingCandidateId; this.reviewBoard = reviewBoard; this.decision = decision; this.reviewer = reviewer; this.rationale = rationale; this.decidedAt = decidedAt; this.reviewerUserId = reviewerUserId;
    }

    public UUID getReviewerUserId() { return reviewerUserId; }
}
