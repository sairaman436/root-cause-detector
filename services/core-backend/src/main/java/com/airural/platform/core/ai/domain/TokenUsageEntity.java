/*
 * Purpose: Persists token and cost accounting records.
 * Why it exists: AI governance requires per-user and per-model usage visibility.
 * Architecture fit: Cost and usage ledger for the AI gateway.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for token usage. */
@Entity
@Table(name = "token_usage", schema = "ai")
public class TokenUsageEntity {
    @Id
    private UUID id;
    private UUID userId;
    @Column(nullable = false, length = 120)
    private String modelId;
    @Column(nullable = false)
    private Integer promptTokens;
    @Column(nullable = false)
    private Integer completionTokens;
    @Column(nullable = false)
    private Integer totalTokens;
    @Column(nullable = false)
    private Double estimatedCost;
    @Column(nullable = false)
    private Instant createdAt;

    protected TokenUsageEntity() {}

    public TokenUsageEntity(UUID userId, String modelId, Integer promptTokens, Integer completionTokens, Double estimatedCost) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.modelId = modelId;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        this.estimatedCost = estimatedCost;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String modelId() { return modelId; }
    public Integer totalTokens() { return totalTokens; }
    public Double estimatedCost() { return estimatedCost; }
    public Instant createdAt() { return createdAt; }
}
