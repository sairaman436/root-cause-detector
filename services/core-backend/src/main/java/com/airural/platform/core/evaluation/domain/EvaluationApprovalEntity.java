/*
 * Purpose: Stores independent evaluation review board decisions.
 * Why it exists: AI-5 requires Evaluation, Safety, Government Policy, Architecture, and Release Board gates before promotion.
 * Architecture fit: Immutable approval entity for evaluation governance.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Evaluation approval entity. */
@Entity
@Table(name = "evaluation_approvals", schema = "evaluation")
public class EvaluationApprovalEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private String reviewBoard;
    private String status;
    private String reviewer;
    @Column(columnDefinition = "TEXT")
    private String decisionNotes;
    private Instant decidedAt;

    protected EvaluationApprovalEntity() {}

    /** Creates an evaluation approval. */
    public EvaluationApprovalEntity(UUID id, UUID evaluationRunId, String reviewBoard, String status, String reviewer, String decisionNotes, Instant decidedAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.reviewBoard = reviewBoard; this.status = status; this.reviewer = reviewer; this.decisionNotes = decisionNotes; this.decidedAt = decidedAt;
    }
}
