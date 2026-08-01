/*
 * Purpose: Stores training artifact metadata and integrity checks.
 * Why it exists: Logs, configs, adapters, manifests, and future model outputs need traceable storage URIs and checksums.
 * Architecture fit: AI-3 artifact store entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Training artifact entity. */
@Entity
@Table(name = "training_artifacts", schema = "training")
public class TrainingArtifactEntity {
    @Id private UUID id;
    private UUID jobId;
    private UUID runId;
    private String artifactType;
    private String storageUri;
    private String checksum;
    private Long sizeBytes;
    private String integrityStatus;
    private Instant createdAt;

    protected TrainingArtifactEntity() {}

    /** Creates a training artifact record. */
    public TrainingArtifactEntity(UUID id, UUID jobId, UUID runId, String artifactType, String storageUri, String checksum, Long sizeBytes, String integrityStatus, Instant createdAt) {
        this.id = id; this.jobId = jobId; this.runId = runId; this.artifactType = artifactType; this.storageUri = storageUri; this.checksum = checksum; this.sizeBytes = sizeBytes; this.integrityStatus = integrityStatus; this.createdAt = createdAt;
    }
}
