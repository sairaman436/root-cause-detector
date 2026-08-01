/*
 * Purpose: Stores supervised fine-tuning training metrics.
 * Why it exists: AI-4 must track loss, learning rate, GPU utilization, VRAM, checkpoint progress, training time, and validation loss.
 * Architecture fit: Observability entity for fine-tuning lifecycle metrics.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Fine-tuning training metrics entity. */
@Entity
@Table(name = "training_metrics", schema = "finetuning")
public class FineTuningTrainingMetricsEntity {
    @Id private UUID id;
    private UUID runId;
    private BigDecimal lossValue;
    private BigDecimal validationLoss;
    private BigDecimal learningRate;
    private BigDecimal gpuUtilization;
    private BigDecimal vramUsageGb;
    private BigDecimal checkpointProgress;
    private Long trainingTimeSeconds;
    private Instant recordedAt;

    protected FineTuningTrainingMetricsEntity() {}

    /** Creates a fine-tuning training metrics point. */
    public FineTuningTrainingMetricsEntity(UUID id, UUID runId, BigDecimal lossValue, BigDecimal validationLoss, BigDecimal learningRate, BigDecimal gpuUtilization, BigDecimal vramUsageGb, BigDecimal checkpointProgress, Long trainingTimeSeconds, Instant recordedAt) {
        this.id = id; this.runId = runId; this.lossValue = lossValue; this.validationLoss = validationLoss; this.learningRate = learningRate; this.gpuUtilization = gpuUtilization; this.vramUsageGb = vramUsageGb; this.checkpointProgress = checkpointProgress; this.trainingTimeSeconds = trainingTimeSeconds; this.recordedAt = recordedAt;
    }
}
