/*
 * Purpose: Stores normalized knowledge datasets derived from trusted sources.
 * Why it exists: RAG, evaluation, synthetic data, and policy retrieval require versioned knowledge corpora.
 * Architecture fit: Dataset registry entity for the AI-2 knowledge factory.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Knowledge dataset registry entity. */
@Entity
@Table(name = "knowledge_datasets", schema = "knowledge")
public class KnowledgeDatasetEntity {
    @Id private UUID id;
    private UUID sourceId;
    private String name;
    private String datasetType;
    private String status;
    private String ownerTeam;
    private String retentionPolicy;
    private Integer versionNumber;
    private Instant createdAt;
    private Instant updatedAt;

    protected KnowledgeDatasetEntity() {}

    /** Creates a knowledge dataset. */
    public KnowledgeDatasetEntity(UUID id, UUID sourceId, String name, String datasetType, String status, String ownerTeam, String retentionPolicy, Integer versionNumber, Instant createdAt, Instant updatedAt) {
        this.id = id; this.sourceId = sourceId; this.name = name; this.datasetType = datasetType; this.status = status; this.ownerTeam = ownerTeam; this.retentionPolicy = retentionPolicy; this.versionNumber = versionNumber; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getSourceId() { return sourceId; }
    public String getName() { return name; }
    public String getDatasetType() { return datasetType; }
    public String getStatus() { return status; }
    public String getOwnerTeam() { return ownerTeam; }
    public String getRetentionPolicy() { return retentionPolicy; }
    public Integer getVersionNumber() { return versionNumber; }
}
