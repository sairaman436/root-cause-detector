/*
 * Purpose: Stores human review decisions for feedback records.
 * Why it exists: AI-7 requires accept, edit, reject, quality validation, and escalation before dataset eligibility.
 * Architecture fit: Governance workflow evidence for learning records.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Human review entity. */
@Entity
@Table(name = "human_reviews", schema = "learning")
public class HumanReviewEntity {
    @Id private UUID id;
    private UUID learningRecordId;
    private String reviewer;
    private String decision;
    private String escalationLevel;
    @Column(columnDefinition = "TEXT") private String comments;
    private Instant reviewedAt;

    protected HumanReviewEntity() {}

    /** Creates a human review. */
    public HumanReviewEntity(UUID id, UUID learningRecordId, String reviewer, String decision, String escalationLevel, String comments, Instant reviewedAt) {
        this.id = id; this.learningRecordId = learningRecordId; this.reviewer = reviewer; this.decision = decision; this.escalationLevel = escalationLevel; this.comments = comments; this.reviewedAt = reviewedAt;
    }
}
