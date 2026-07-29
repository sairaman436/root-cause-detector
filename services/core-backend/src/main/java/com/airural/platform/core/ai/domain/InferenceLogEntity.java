/*
 * Purpose: Records AI gateway inference attempts.
 * Why it exists: Latency, safety, usage, audit, and troubleshooting need durable inference telemetry.
 * Architecture fit: Observability and governance entity for model serving.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for inference logs. */
@Entity
@Table(name = "inference_logs", schema = "ai")
public class InferenceLogEntity {
    @Id
    private UUID id;
    private UUID userId;
    @Column(nullable = false, length = 120)
    private String modelId;
    @Column(nullable = false, length = 40)
    private String requestType;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(nullable = false)
    private Integer promptTokens;
    @Column(nullable = false)
    private Integer completionTokens;
    @Column(nullable = false)
    private Long latencyMs;
    @Column(nullable = false)
    private Boolean safetyBlocked;
    @Column(columnDefinition = "TEXT")
    private String promptHash;
    @Column(columnDefinition = "TEXT")
    private String responsePreview;
    @Column(nullable = false)
    private Instant createdAt;

    protected InferenceLogEntity() {}

    public InferenceLogEntity(UUID userId, String modelId, String requestType, String status, Integer promptTokens, Integer completionTokens, Long latencyMs, Boolean safetyBlocked, String promptHash, String responsePreview) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.modelId = modelId;
        this.requestType = requestType;
        this.status = status;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.safetyBlocked = safetyBlocked;
        this.promptHash = promptHash;
        this.responsePreview = responsePreview;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID userId() { return userId; }
    public String modelId() { return modelId; }
    public String requestType() { return requestType; }
    public String status() { return status; }
    public Integer promptTokens() { return promptTokens; }
    public Integer completionTokens() { return completionTokens; }
    public Long latencyMs() { return latencyMs; }
    public Boolean safetyBlocked() { return safetyBlocked; }
    public Instant createdAt() { return createdAt; }
}
