/*
 * Purpose: Stores immutable dataset versions.
 * Why it exists: Dataset rollback, lineage, approval, and reproducible training/evaluation require versioned artifacts.
 * Architecture fit: Dataset registry version table for AI-1.
 */
package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Dataset version entity. */
@Entity
@Table(name = "dataset_versions", schema = "datasets")
public class DatasetVersionEntity {
    @Id private UUID id;
    private UUID datasetId;
    private Integer versionNumber;
    private String status;
    private String storageUri;
    private String checksum;
    private Instant createdAt;

    protected DatasetVersionEntity() {}

    public DatasetVersionEntity(UUID id, UUID datasetId, Integer versionNumber, String status, String storageUri, String checksum, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.versionNumber = versionNumber; this.status = status; this.storageUri = storageUri; this.checksum = checksum; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getDatasetId() { return datasetId; }
    public Integer getVersionNumber() { return versionNumber; }
    public String getStatus() { return status; }
    public String getStorageUri() { return storageUri; }
    public String getChecksum() { return checksum; }
    public Instant getCreatedAt() { return createdAt; }
}
