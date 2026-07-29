/*
 * Purpose: Persists citations attached to RAG answers.
 * Why it exists: Every RAG response must be traceable to retrieved knowledge sources.
 * Architecture fit: Citation child entity for RAG governance and explainability.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.util.UUID;

/** JPA entity for RAG citations. */
@Entity
@Table(name = "rag_citations", schema = "ai")
public class RAGCitationEntity {
    @Id
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rag_request_id")
    private RAGRequestEntity ragRequest;
    @Column(nullable = false, length = 120)
    private String sourceType;
    @Column(nullable = false, length = 180)
    private String sourceId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String excerpt;
    @Column(nullable = false)
    private Double score;

    protected RAGCitationEntity() {}

    public RAGCitationEntity(RAGRequestEntity ragRequest, String sourceType, String sourceId, String excerpt, Double score) {
        this.id = UUID.randomUUID();
        this.ragRequest = ragRequest;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.excerpt = excerpt;
        this.score = score;
    }

    public UUID id() { return id; }
    public String sourceType() { return sourceType; }
    public String sourceId() { return sourceId; }
    public String excerpt() { return excerpt; }
    public Double score() { return score; }
}
