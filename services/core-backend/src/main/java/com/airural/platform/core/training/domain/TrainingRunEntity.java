/*
 * Purpose: Stores lifecycle records for scheduled training runs.
 * Why it exists: Resume, cancellation, duration tracking, and auditability require run-level records separate from job definitions.
 * Architecture fit: AI-3 training scheduler entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Training run entity. */
@Entity
@Table(name = "training_runs", schema = "training")
public class TrainingRunEntity {
    @Id private UUID id;
    private UUID jobId;
    private UUID gpuResourceId;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationSeconds;
    private String schedulerDecision;

    protected TrainingRunEntity() {}

    /** Creates a training run record. */
    public TrainingRunEntity(UUID id, UUID jobId, UUID gpuResourceId, String status, Instant startedAt, Instant completedAt, Long durationSeconds, String schedulerDecision) {
        this.id = id; this.jobId = jobId; this.gpuResourceId = gpuResourceId; this.status = status; this.startedAt = startedAt; this.completedAt = completedAt; this.durationSeconds = durationSeconds; this.schedulerDecision = schedulerDecision;
    }

    public UUID getId() { return id; }
    public UUID getJobId() { return jobId; }
    public String getStatus() { return status; }
}
