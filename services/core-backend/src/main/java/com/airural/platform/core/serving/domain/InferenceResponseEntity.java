/*
 * Purpose: Stores model output validation, citation validation, cache, fallback, and token telemetry.
 * Why it exists: Serving responses must be auditable and validated before returning to clients.
 * Architecture fit: Response record paired with inference requests.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Inference response entity. */
@Entity
@Table(name = "inference_responses", schema = "serving")
public class InferenceResponseEntity {
    @Id private UUID id;
    private UUID inferenceRequestId;
    private String selectedModel;
    private String provider;
    @Column(columnDefinition = "TEXT") private String outputText;
    private String outputValidationStatus;
    private String citationValidationStatus;
    private Boolean fallbackUsed;
    private Boolean cacheHit;
    private Integer promptTokens;
    private Integer completionTokens;
    private Long latencyMs;
    private String status;
    private Instant createdAt;

    protected InferenceResponseEntity() {}

    /** Creates an inference response. */
    public InferenceResponseEntity(UUID id, UUID inferenceRequestId, String selectedModel, String provider, String outputText, String outputValidationStatus, String citationValidationStatus, Boolean fallbackUsed, Boolean cacheHit, Integer promptTokens, Integer completionTokens, Long latencyMs, String status, Instant createdAt) {
        this.id = id; this.inferenceRequestId = inferenceRequestId; this.selectedModel = selectedModel; this.provider = provider; this.outputText = outputText; this.outputValidationStatus = outputValidationStatus; this.citationValidationStatus = citationValidationStatus; this.fallbackUsed = fallbackUsed; this.cacheHit = cacheHit; this.promptTokens = promptTokens; this.completionTokens = completionTokens; this.latencyMs = latencyMs; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
}
