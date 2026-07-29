/*
 * Purpose: Persists RAG request metadata and response summary.
 * Why it exists: Retrieval-augmented generation must be auditable, measurable, and citable.
 * Architecture fit: RAG pipeline record for AI governance.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for RAG requests. */
@Entity
@Table(name = "rag_requests", schema = "ai")
public class RAGRequestEntity {
    @Id
    private UUID id;
    private UUID userId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String queryText;
    @Column(nullable = false, length = 120)
    private String collectionName;
    @Column(nullable = false, length = 120)
    private String modelId;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseText;
    @Column(nullable = false)
    private Long retrievalLatencyMs;
    @Column(nullable = false)
    private Long inferenceLatencyMs;
    @Column(nullable = false)
    private Instant createdAt;

    protected RAGRequestEntity() {}

    public RAGRequestEntity(UUID userId, String queryText, String collectionName, String modelId, String status, String responseText, Long retrievalLatencyMs, Long inferenceLatencyMs) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.queryText = queryText;
        this.collectionName = collectionName;
        this.modelId = modelId;
        this.status = status;
        this.responseText = responseText;
        this.retrievalLatencyMs = retrievalLatencyMs;
        this.inferenceLatencyMs = inferenceLatencyMs;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String queryText() { return queryText; }
    public String responseText() { return responseText; }
    public String status() { return status; }
    public Instant createdAt() { return createdAt; }
}
