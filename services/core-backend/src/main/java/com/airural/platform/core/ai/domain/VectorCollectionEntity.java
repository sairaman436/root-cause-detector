/*
 * Purpose: Registers vector collections managed by the AI foundation.
 * Why it exists: RAG, embeddings, and future memory need governed collection metadata.
 * Architecture fit: Qdrant collection catalog mirrored in PostgreSQL.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for vector collection metadata. */
@Entity
@Table(name = "vector_collections", schema = "ai")
public class VectorCollectionEntity {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true, length = 120)
    private String name;
    @Column(nullable = false)
    private Integer vectorSize;
    @Column(nullable = false, length = 80)
    private String distanceMetric;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private Instant createdAt;

    protected VectorCollectionEntity() {}

    public VectorCollectionEntity(String name, Integer vectorSize, String distanceMetric, String status, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.vectorSize = vectorSize;
        this.distanceMetric = distanceMetric;
        this.status = status;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public Integer vectorSize() { return vectorSize; }
    public String distanceMetric() { return distanceMetric; }
    public String status() { return status; }
}
