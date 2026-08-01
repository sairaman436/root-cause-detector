/*
 * Purpose: Stores immutable model evaluation runs.
 * Why it exists: Every model must have reproducible, versioned evaluation history before promotion decisions.
 * Architecture fit: Primary operational entity for AI-5.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Evaluation run entity. */
@Entity
@Table(name = "evaluation_runs", schema = "evaluation")
public class EvaluationRunEntity {
    @Id private UUID id;
    private UUID modelRunId;
    private String modelName;
    private String modelFamily;
    private String evaluationType;
    private String status;
    private String recommendation;
    private BigDecimal overallScore;
    private String immutableHash;
    @Column(columnDefinition = "TEXT")
    private String auditJson;
    private Instant startedAt;
    private Instant completedAt;

    protected EvaluationRunEntity() {}

    /** Creates an immutable evaluation run. */
    public EvaluationRunEntity(UUID id, UUID modelRunId, String modelName, String modelFamily, String evaluationType, String status, String recommendation, BigDecimal overallScore, String immutableHash, String auditJson, Instant startedAt, Instant completedAt) {
        this.id = id; this.modelRunId = modelRunId; this.modelName = modelName; this.modelFamily = modelFamily; this.evaluationType = evaluationType; this.status = status; this.recommendation = recommendation; this.overallScore = overallScore; this.immutableHash = immutableHash; this.auditJson = auditJson; this.startedAt = startedAt; this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public UUID getModelRunId() { return modelRunId; }
    public String getModelName() { return modelName; }
    public String getModelFamily() { return modelFamily; }
    public String getStatus() { return status; }
    public String getRecommendation() { return recommendation; }
    public BigDecimal getOverallScore() { return overallScore; }
}
