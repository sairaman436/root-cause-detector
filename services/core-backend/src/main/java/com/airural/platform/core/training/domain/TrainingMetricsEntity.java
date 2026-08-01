/*
 * Purpose: Stores training telemetry and dashboard metrics.
 * Why it exists: Operators need GPU utilization, VRAM, throughput, loss, checkpoint cadence, duration, and resource consumption observability.
 * Architecture fit: AI-3 metrics tracking entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Training metrics entity. */
@Entity
@Table(name = "training_metrics", schema = "training")
public class TrainingMetricsEntity {
    @Id private UUID id;
    private UUID jobId;
    private UUID runId;
    private BigDecimal gpuUtilization;
    private BigDecimal vramUsageGb;
    private BigDecimal trainingThroughput;
    private BigDecimal lossValue;
    private Integer checkpointCount;
    private BigDecimal estimatedCost;
    private Instant recordedAt;

    protected TrainingMetricsEntity() {}

    /** Creates a training metrics point. */
    public TrainingMetricsEntity(UUID id, UUID jobId, UUID runId, BigDecimal gpuUtilization, BigDecimal vramUsageGb, BigDecimal trainingThroughput, BigDecimal lossValue, Integer checkpointCount, BigDecimal estimatedCost, Instant recordedAt) {
        this.id = id; this.jobId = jobId; this.runId = runId; this.gpuUtilization = gpuUtilization; this.vramUsageGb = vramUsageGb; this.trainingThroughput = trainingThroughput; this.lossValue = lossValue; this.checkpointCount = checkpointCount; this.estimatedCost = estimatedCost; this.recordedAt = recordedAt;
    }
}
