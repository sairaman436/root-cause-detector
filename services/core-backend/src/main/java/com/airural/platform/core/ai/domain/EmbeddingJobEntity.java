/*
 * Purpose: Tracks embedding pipeline work.
 * Why it exists: Batch embedding, re-embedding, retries, and observability require durable job state.
 * Architecture fit: AI foundation pipeline entity for asynchronous vector preparation.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for embedding jobs. */
@Entity
@Table(name = "embedding_jobs", schema = "ai")
public class EmbeddingJobEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 80)
    private String sourceType;
    @Column(nullable = false)
    private UUID sourceId;
    @Column(nullable = false, length = 120)
    private String embeddingModel;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(nullable = false)
    private Integer chunkCount;
    @Column(length = 1000)
    private String errorMessage;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant completedAt;

    protected EmbeddingJobEntity() {}

    public EmbeddingJobEntity(String sourceType, UUID sourceId, String embeddingModel, Integer chunkCount) {
        this.id = UUID.randomUUID();
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.embeddingModel = embeddingModel;
        this.status = "COMPLETED";
        this.chunkCount = chunkCount;
        this.createdAt = Instant.now();
        this.completedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public String sourceType() { return sourceType; }
    public UUID sourceId() { return sourceId; }
    public String embeddingModel() { return embeddingModel; }
    public String status() { return status; }
    public Integer chunkCount() { return chunkCount; }
}
