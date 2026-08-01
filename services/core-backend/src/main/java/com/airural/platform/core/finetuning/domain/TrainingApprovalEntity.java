/*
 * Purpose: Stores final review board approvals for fine-tuning releases.
 * Why it exists: AI-4 requires Architecture, AI Research, MLOps, Security, Performance, External Audit, and Release Review approval before acceptance.
 * Architecture fit: Governance approval entity for fine-tuning lifecycle gates.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Fine-tuning approval entity. */
@Entity
@Table(name = "training_approvals", schema = "finetuning")
public class TrainingApprovalEntity {
    @Id private UUID id;
    private UUID runId;
    private String reviewBoard;
    private String status;
    private String reviewer;
    @Column(columnDefinition = "TEXT")
    private String decisionNotes;
    private Instant decidedAt;

    protected TrainingApprovalEntity() {}

    /** Creates a review approval record. */
    public TrainingApprovalEntity(UUID id, UUID runId, String reviewBoard, String status, String reviewer, String decisionNotes, Instant decidedAt) {
        this.id = id; this.runId = runId; this.reviewBoard = reviewBoard; this.status = status; this.reviewer = reviewer; this.decisionNotes = decisionNotes; this.decidedAt = decidedAt;
    }
}
