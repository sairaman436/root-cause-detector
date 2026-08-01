package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Dataset lineage edge from source systems to derived datasets. */
@Entity
@Table(name = "dataset_lineage", schema = "datasets")
public class DatasetLineageEntity {
    @Id private UUID id;
    private UUID datasetId;
    private String sourceType;
    private UUID sourceId;
    private String transformation;
    private Instant createdAt;
    protected DatasetLineageEntity() {}
    public DatasetLineageEntity(UUID id, UUID datasetId, String sourceType, UUID sourceId, String transformation, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.sourceType = sourceType; this.sourceId = sourceId; this.transformation = transformation; this.createdAt = createdAt;
    }
}
