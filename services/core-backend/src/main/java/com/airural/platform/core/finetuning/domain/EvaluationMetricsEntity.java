/*
 * Purpose: Stores post-training evaluation quality gates.
 * Why it exists: AI-4 must evaluate reasoning, hallucination, safety, citations, policy compliance, formatting, latency, and memory use before release review.
 * Architecture fit: Evaluation entity for the first Rural Intelligence Foundation Model adapter.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Fine-tuning evaluation metrics entity. */
@Entity
@Table(name = "evaluation_metrics", schema = "finetuning")
public class EvaluationMetricsEntity {
    @Id private UUID id;
    private UUID runId;
    private BigDecimal reasoningScore;
    private BigDecimal hallucinationScore;
    private BigDecimal safetyScore;
    private BigDecimal citationAccuracy;
    private BigDecimal policyCompliance;
    private BigDecimal outputFormatting;
    private BigDecimal latencyMs;
    private BigDecimal memoryUsageGb;
    private BigDecimal overallScore;
    private Instant createdAt;

    protected EvaluationMetricsEntity() {}

    /** Creates evaluation metrics. */
    public EvaluationMetricsEntity(UUID id, UUID runId, BigDecimal reasoningScore, BigDecimal hallucinationScore, BigDecimal safetyScore, BigDecimal citationAccuracy, BigDecimal policyCompliance, BigDecimal outputFormatting, BigDecimal latencyMs, BigDecimal memoryUsageGb, BigDecimal overallScore, Instant createdAt) {
        this.id = id; this.runId = runId; this.reasoningScore = reasoningScore; this.hallucinationScore = hallucinationScore; this.safetyScore = safetyScore; this.citationAccuracy = citationAccuracy; this.policyCompliance = policyCompliance; this.outputFormatting = outputFormatting; this.latencyMs = latencyMs; this.memoryUsageGb = memoryUsageGb; this.overallScore = overallScore; this.createdAt = createdAt;
    }
}
