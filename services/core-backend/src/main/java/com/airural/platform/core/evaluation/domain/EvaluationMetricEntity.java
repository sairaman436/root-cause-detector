/*
 * Purpose: Stores model evaluation observability metrics.
 * Why it exists: AI-5 tracks accuracy, precision, recall, F1, hallucination rate, citations, latency, VRAM, GPU time, token usage, and reasoning quality.
 * Architecture fit: Metrics entity for reproducible model evaluation.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Evaluation metric entity. */
@Entity
@Table(name = "evaluation_metrics", schema = "evaluation")
public class EvaluationMetricEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private BigDecimal accuracy;
    private BigDecimal precisionScore;
    private BigDecimal recallScore;
    @Column(name = "f1_score")
    private BigDecimal f1Score;
    private BigDecimal hallucinationRate;
    private BigDecimal citationAccuracy;
    private BigDecimal latencyMs;
    private BigDecimal vramGb;
    private BigDecimal gpuTimeSeconds;
    private Integer tokenUsage;
    private BigDecimal reasoningQuality;
    private Instant createdAt;

    protected EvaluationMetricEntity() {}

    /** Creates evaluation metrics. */
    public EvaluationMetricEntity(UUID id, UUID evaluationRunId, BigDecimal accuracy, BigDecimal precisionScore, BigDecimal recallScore, BigDecimal f1Score, BigDecimal hallucinationRate, BigDecimal citationAccuracy, BigDecimal latencyMs, BigDecimal vramGb, BigDecimal gpuTimeSeconds, Integer tokenUsage, BigDecimal reasoningQuality, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.accuracy = accuracy; this.precisionScore = precisionScore; this.recallScore = recallScore; this.f1Score = f1Score; this.hallucinationRate = hallucinationRate; this.citationAccuracy = citationAccuracy; this.latencyMs = latencyMs; this.vramGb = vramGb; this.gpuTimeSeconds = gpuTimeSeconds; this.tokenUsage = tokenUsage; this.reasoningQuality = reasoningQuality; this.createdAt = createdAt;
    }
}
