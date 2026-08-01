package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Quality report for dataset validation and approval. */
@Entity
@Table(name = "dataset_quality", schema = "datasets")
public class DatasetQualityEntity {
    @Id private UUID id;
    private UUID datasetId;
    private BigDecimal qualityScore;
    private BigDecimal duplicateRate;
    private BigDecimal piiRate;
    private BigDecimal validationErrorRate;
    @Column(columnDefinition = "TEXT")
    private String findingsJson;
    private Instant createdAt;
    protected DatasetQualityEntity() {}
    public DatasetQualityEntity(UUID id, UUID datasetId, BigDecimal qualityScore, BigDecimal duplicateRate, BigDecimal piiRate, BigDecimal validationErrorRate, String findingsJson, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.qualityScore = qualityScore; this.duplicateRate = duplicateRate; this.piiRate = piiRate; this.validationErrorRate = validationErrorRate; this.findingsJson = findingsJson; this.createdAt = createdAt;
    }
    public BigDecimal getQualityScore() { return qualityScore; }
    public String getFindingsJson() { return findingsJson; }
}
