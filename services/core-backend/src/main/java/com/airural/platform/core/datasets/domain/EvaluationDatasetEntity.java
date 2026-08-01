package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Evaluation dataset registry entry. */
@Entity
@Table(name = "evaluation_datasets", schema = "datasets")
public class EvaluationDatasetEntity {
    @Id private UUID id;
    private UUID datasetId;
    private String benchmarkName;
    private String evaluationPurpose;
    private Integer sampleCount;
    private Instant createdAt;
    protected EvaluationDatasetEntity() {}
    public EvaluationDatasetEntity(UUID id, UUID datasetId, String benchmarkName, String evaluationPurpose, Integer sampleCount, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.benchmarkName = benchmarkName; this.evaluationPurpose = evaluationPurpose; this.sampleCount = sampleCount; this.createdAt = createdAt;
    }
}
