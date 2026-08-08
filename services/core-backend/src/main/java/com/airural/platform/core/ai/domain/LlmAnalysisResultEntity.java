/*
 * Purpose: Persists validated local LLM analysis results and operational metadata.
 * Why it exists: Root-cause analysis must be durable, auditable, and traceable to provider, model, prompt, survey, submission, and request metadata without storing sensitive prompt text.
 * Architecture fit: AI bounded-context aggregate for provider-neutral local inference results.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for local LLM analysis results. */
@Entity
@Table(name = "llm_analysis_results", schema = "ai")
public class LlmAnalysisResultEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID requestId;

    @Column(nullable = false)
    private UUID surveyId;

    private UUID submissionId;

    @Column(nullable = false)
    private UUID requestedByUserId;

    @Column(nullable = false, length = 80)
    private String provider;

    @Column(nullable = false, length = 180)
    private String model;

    @Column(length = 180)
    private String modelVersion;

    @Column(nullable = false, length = 120)
    private String promptId;

    @Column(nullable = false, length = 40)
    private String promptVersion;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(nullable = false)
    private Long latencyMs;

    @Column(nullable = false)
    private Integer tokensEstimate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultJson;

    @Column(length = 120)
    private String errorCode;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    protected LlmAnalysisResultEntity() {
    }

    public LlmAnalysisResultEntity(
            UUID requestId,
            UUID surveyId,
            UUID submissionId,
            UUID requestedByUserId,
            String provider,
            String model,
            String modelVersion,
            String promptId,
            String promptVersion,
            String status,
            Long latencyMs,
            Integer tokensEstimate,
            String resultJson,
            String errorCode,
            String errorMessage) {
        this.id = UUID.randomUUID();
        this.requestId = requestId;
        this.surveyId = surveyId;
        this.submissionId = submissionId;
        this.requestedByUserId = requestedByUserId;
        this.provider = provider;
        this.model = model;
        this.modelVersion = modelVersion;
        this.promptId = promptId;
        this.promptVersion = promptVersion;
        this.status = status;
        this.latencyMs = latencyMs;
        this.tokensEstimate = tokensEstimate;
        this.resultJson = resultJson;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID requestId() { return requestId; }
    public UUID surveyId() { return surveyId; }
    public UUID submissionId() { return submissionId; }
    public UUID requestedByUserId() { return requestedByUserId; }
    public String provider() { return provider; }
    public String model() { return model; }
    public String modelVersion() { return modelVersion; }
    public String promptId() { return promptId; }
    public String promptVersion() { return promptVersion; }
    public String status() { return status; }
    public Long latencyMs() { return latencyMs; }
    public Integer tokensEstimate() { return tokensEstimate; }
    public String resultJson() { return resultJson; }
    public String errorCode() { return errorCode; }
    public String errorMessage() { return errorMessage; }
    public Instant createdAt() { return createdAt; }
}
