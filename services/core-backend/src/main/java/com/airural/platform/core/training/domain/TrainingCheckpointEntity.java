/*
 * Purpose: Stores checkpoint metadata for resume and recovery.
 * Why it exists: Training jobs must support automatic checkpoints, validation, restore, comparison, and cleanup.
 * Architecture fit: AI-3 checkpoint manager entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Training checkpoint entity. */
@Entity
@Table(name = "training_checkpoints", schema = "training")
public class TrainingCheckpointEntity {
    @Id private UUID id;
    private UUID jobId;
    private UUID runId;
    private Integer checkpointStep;
    private String checkpointType;
    private String storageUri;
    private String checksum;
    private String validationStatus;
    private Boolean restorable;
    private Instant createdAt;

    protected TrainingCheckpointEntity() {}

    /** Creates a checkpoint record. */
    public TrainingCheckpointEntity(UUID id, UUID jobId, UUID runId, Integer checkpointStep, String checkpointType, String storageUri, String checksum, String validationStatus, Boolean restorable, Instant createdAt) {
        this.id = id; this.jobId = jobId; this.runId = runId; this.checkpointStep = checkpointStep; this.checkpointType = checkpointType; this.storageUri = storageUri; this.checksum = checksum; this.validationStatus = validationStatus; this.restorable = restorable; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getJobId() { return jobId; }
    public String getValidationStatus() { return validationStatus; }
    public Boolean getRestorable() { return restorable; }
}
