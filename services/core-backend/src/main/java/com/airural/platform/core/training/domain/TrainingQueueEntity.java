/*
 * Purpose: Stores queue entries for validated training jobs.
 * Why it exists: Job priority, scheduling fairness, and retry controls need a durable queue before workers exist.
 * Architecture fit: AI-3 training queue entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Training queue entity. */
@Entity
@Table(name = "training_queue", schema = "training")
public class TrainingQueueEntity {
    @Id private UUID id;
    private UUID jobId;
    private Integer priority;
    private String queueStatus;
    private Integer attemptCount;
    private Instant availableAt;
    private Instant createdAt;

    protected TrainingQueueEntity() {}

    /** Creates a training queue entry. */
    public TrainingQueueEntity(UUID id, UUID jobId, Integer priority, String queueStatus, Integer attemptCount, Instant availableAt, Instant createdAt) {
        this.id = id; this.jobId = jobId; this.priority = priority; this.queueStatus = queueStatus; this.attemptCount = attemptCount; this.availableAt = availableAt; this.createdAt = createdAt;
    }
}
