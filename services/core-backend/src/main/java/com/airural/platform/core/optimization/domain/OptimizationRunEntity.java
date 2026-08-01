/*
 * Purpose: Stores an immutable model optimization job created after evaluation approval.
 * Why it exists: AI-6 needs a durable root record for export, quantization, packaging, benchmark, and release evidence.
 * Architecture fit: Primary aggregate for model optimization and packaging readiness.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Optimization run entity. */
@Entity
@Table(name = "optimization_runs", schema = "optimization")
public class OptimizationRunEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private UUID modelRunId;
    private String modelName;
    private String modelFamily;
    private String status;
    private String releaseRecommendation;
    private String immutableHash;
    @Column(columnDefinition = "TEXT")
    private String requestedFormatsJson;
    @Column(columnDefinition = "TEXT")
    private String requestedTargetsJson;
    private Instant startedAt;
    private Instant completedAt;

    protected OptimizationRunEntity() {}

    /** Creates an optimization run. */
    public OptimizationRunEntity(UUID id, UUID evaluationRunId, UUID modelRunId, String modelName, String modelFamily, String status, String releaseRecommendation, String immutableHash, String requestedFormatsJson, String requestedTargetsJson, Instant startedAt, Instant completedAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.modelRunId = modelRunId; this.modelName = modelName; this.modelFamily = modelFamily; this.status = status; this.releaseRecommendation = releaseRecommendation; this.immutableHash = immutableHash; this.requestedFormatsJson = requestedFormatsJson; this.requestedTargetsJson = requestedTargetsJson; this.startedAt = startedAt; this.completedAt = completedAt;
    }

    public UUID getId() { return id; }
    public UUID getEvaluationRunId() { return evaluationRunId; }
    public UUID getModelRunId() { return modelRunId; }
    public String getModelName() { return modelName; }
    public String getModelFamily() { return modelFamily; }
    public String getStatus() { return status; }
    public String getReleaseRecommendation() { return releaseRecommendation; }
}
