/*
 * Purpose: Stores governed AI dataset registry records.
 * Why it exists: Fine-tuning, evaluation, RAG, and agent memory datasets require ownership, lifecycle, and quality metadata.
 * Architecture fit: Operational entity for the AI-1 Dataset Engineering Platform.
 */
package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Dataset registry entity. */
@Entity
@Table(name = "datasets", schema = "datasets")
public class DatasetEntity {
    @Id
    private UUID id;
    private String name;
    private String datasetType;
    private String status;
    private UUID ownerId;
    private String description;
    private String tags;
    @Column(columnDefinition = "TEXT")
    private String metadataJson;
    private BigDecimal qualityScore;
    private BigDecimal syntheticRatio;
    private Instant createdAt;
    private Instant updatedAt;

    protected DatasetEntity() {
    }

    /** Creates a dataset registry entry. */
    public DatasetEntity(UUID id, String name, String datasetType, String status, UUID ownerId, String description, String tags, String metadataJson, BigDecimal qualityScore, BigDecimal syntheticRatio, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.datasetType = datasetType;
        this.status = status;
        this.ownerId = ownerId;
        this.description = description;
        this.tags = tags;
        this.metadataJson = metadataJson;
        this.qualityScore = qualityScore;
        this.syntheticRatio = syntheticRatio;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDatasetType() { return datasetType; }
    public String getStatus() { return status; }
    public UUID getOwnerId() { return ownerId; }
    public String getDescription() { return description; }
    public String getTags() { return tags; }
    public String getMetadataJson() { return metadataJson; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public BigDecimal getSyntheticRatio() { return syntheticRatio; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /** Updates lifecycle status and quality values after validation. */
    public void updateQuality(String status, BigDecimal qualityScore, BigDecimal syntheticRatio) {
        this.status = status;
        this.qualityScore = qualityScore;
        this.syntheticRatio = syntheticRatio;
        this.updatedAt = Instant.now();
    }
}
