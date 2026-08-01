package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Synthetic dataset generation record with required synthetic marking. */
@Entity
@Table(name = "synthetic_datasets", schema = "datasets")
public class SyntheticDatasetEntity {
    @Id private UUID id;
    private UUID datasetId;
    private String generationMethod;
    private String safetyStatus;
    private Integer sampleCount;
    @Column(columnDefinition = "TEXT")
    private String provenanceJson;
    private Instant createdAt;
    protected SyntheticDatasetEntity() {}
    public SyntheticDatasetEntity(UUID id, UUID datasetId, String generationMethod, String safetyStatus, Integer sampleCount, String provenanceJson, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.generationMethod = generationMethod; this.safetyStatus = safetyStatus; this.sampleCount = sampleCount; this.provenanceJson = provenanceJson; this.createdAt = createdAt;
    }
}
