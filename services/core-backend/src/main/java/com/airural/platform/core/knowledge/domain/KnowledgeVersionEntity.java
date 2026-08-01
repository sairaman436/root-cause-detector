/*
 * Purpose: Stores immutable knowledge dataset versions.
 * Why it exists: Corpora must support version tracking, rollback, lineage, and reproducible RAG indexes.
 * Architecture fit: Version registry entity for AI-2.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Knowledge version entity. */
@Entity
@Table(name = "knowledge_versions", schema = "knowledge")
public class KnowledgeVersionEntity {
    @Id private UUID id;
    private UUID datasetId;
    private Integer versionNumber;
    private String checksum;
    private String storageUri;
    private String status;
    private Instant createdAt;

    protected KnowledgeVersionEntity() {}

    /** Creates a knowledge dataset version. */
    public KnowledgeVersionEntity(UUID id, UUID datasetId, Integer versionNumber, String checksum, String storageUri, String status, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.versionNumber = versionNumber; this.checksum = checksum; this.storageUri = storageUri; this.status = status; this.createdAt = createdAt;
    }
}
