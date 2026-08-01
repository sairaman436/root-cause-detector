/*
 * Purpose: Stores structured training logs for audit and operations.
 * Why it exists: Training job decisions, guardrail failures, scheduler actions, and checkpoint events must be searchable.
 * Architecture fit: AI-3 training logs entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Structured training log entity. */
@Entity
@Table(name = "training_logs", schema = "training")
public class TrainingLogEntity {
    @Id private UUID id;
    private UUID jobId;
    private String logLevel;
    private String eventType;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Column(columnDefinition = "TEXT")
    private String contextJson;
    private Instant createdAt;

    protected TrainingLogEntity() {}

    /** Creates a structured training log. */
    public TrainingLogEntity(UUID id, UUID jobId, String logLevel, String eventType, String message, String contextJson, Instant createdAt) {
        this.id = id; this.jobId = jobId; this.logLevel = logLevel; this.eventType = eventType; this.message = message; this.contextJson = contextJson; this.createdAt = createdAt;
    }
}
