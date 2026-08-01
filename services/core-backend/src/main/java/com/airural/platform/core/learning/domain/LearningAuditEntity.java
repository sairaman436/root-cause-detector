/*
 * Purpose: Stores immutable audit events for learning workflows.
 * Why it exists: Feedback capture, review, candidate promotion, rejection, and sensitive-data controls must be auditable.
 * Architecture fit: Audit evidence entity for AI-7 governance.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Learning audit entity. */
@Entity
@Table(name = "learning_audits", schema = "learning")
public class LearningAuditEntity {
    @Id private UUID id;
    private UUID learningRecordId;
    private UUID trainingCandidateId;
    private String eventType;
    private String actor;
    @Column(columnDefinition = "TEXT") private String eventJson;
    private String immutableHash;
    private Instant createdAt;

    protected LearningAuditEntity() {}

    /** Creates an immutable audit record. */
    public LearningAuditEntity(UUID id, UUID learningRecordId, UUID trainingCandidateId, String eventType, String actor, String eventJson, String immutableHash, Instant createdAt) {
        this.id = id; this.learningRecordId = learningRecordId; this.trainingCandidateId = trainingCandidateId; this.eventType = eventType; this.actor = actor; this.eventJson = eventJson; this.immutableHash = immutableHash; this.createdAt = createdAt;
    }
}
