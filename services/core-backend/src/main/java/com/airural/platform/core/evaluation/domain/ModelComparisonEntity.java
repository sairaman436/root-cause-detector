/*
 * Purpose: Stores model comparison outcomes.
 * Why it exists: AI-5 must compare candidate adapters against base, previous, production, and experimental adapters and recommend promote, reject, or rollback.
 * Architecture fit: Promotion decision entity for evaluation governance.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Model comparison entity. */
@Entity
@Table(name = "model_comparisons", schema = "evaluation")
public class ModelComparisonEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private String baselineType;
    private String baselineModel;
    private BigDecimal candidateScore;
    private BigDecimal baselineScore;
    private String recommendation;
    @Column(columnDefinition = "TEXT")
    private String comparisonJson;
    private Instant createdAt;

    protected ModelComparisonEntity() {}

    /** Creates a model comparison result. */
    public ModelComparisonEntity(UUID id, UUID evaluationRunId, String baselineType, String baselineModel, BigDecimal candidateScore, BigDecimal baselineScore, String recommendation, String comparisonJson, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.baselineType = baselineType; this.baselineModel = baselineModel; this.candidateScore = candidateScore; this.baselineScore = baselineScore; this.recommendation = recommendation; this.comparisonJson = comparisonJson; this.createdAt = createdAt;
    }

    public UUID getEvaluationRunId() { return evaluationRunId; }
    public String getRecommendation() { return recommendation; }
}
