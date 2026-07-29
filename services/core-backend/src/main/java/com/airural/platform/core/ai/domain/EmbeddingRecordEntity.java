/*
 * Purpose: Stores chunk-level embedding metadata and local vector fallback data.
 * Why it exists: Qdrant is the vector database, but the control plane needs auditable embedding metadata and CI-safe fallback search.
 * Architecture fit: Embedding pipeline record and vector metadata source.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for embedding records. */
@Entity
@Table(name = "embedding_records", schema = "ai")
public class EmbeddingRecordEntity {
    @Id
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private EmbeddingJobEntity job;
    @Column(nullable = false, length = 120)
    private String collectionName;
    @Column(nullable = false)
    private Integer chunkIndex;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String chunkText;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String vectorJson;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String metadataJson;
    @Column(nullable = false)
    private Instant createdAt;

    protected EmbeddingRecordEntity() {}

    public EmbeddingRecordEntity(EmbeddingJobEntity job, String collectionName, Integer chunkIndex, String chunkText, String vectorJson, String metadataJson) {
        this.id = UUID.randomUUID();
        this.job = job;
        this.collectionName = collectionName;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
        this.vectorJson = vectorJson;
        this.metadataJson = metadataJson;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String collectionName() { return collectionName; }
    public Integer chunkIndex() { return chunkIndex; }
    public String chunkText() { return chunkText; }
    public String metadataJson() { return metadataJson; }
}
