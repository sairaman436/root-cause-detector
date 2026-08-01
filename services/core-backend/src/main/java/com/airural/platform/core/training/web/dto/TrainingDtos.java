/*
 * Purpose: Defines public training factory API contracts.
 * Why it exists: Training engineers need stable request and response shapes for jobs, starts, cancellation, experiments, models, and checkpoints.
 * Architecture fit: REST DTO boundary for AI-3 Enterprise Model Training Factory.
 */
package com.airural.platform.core.training.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** DTO namespace for training factory APIs. */
public final class TrainingDtos {
    private TrainingDtos() {
    }

    /** Request to create a governed training job. */
    public record CreateTrainingJobRequest(
            @NotBlank String jobName,
            @NotBlank String baseModel,
            @NotBlank String modelFamily,
            @NotBlank String trainingMethod,
            @NotBlank String datasetSourceType,
            @NotNull UUID datasetId,
            @Min(1) @Max(100) Integer priority,
            @Min(1) @Max(128) Integer requestedGpuCount,
            @Min(1) @Max(4096) Integer requestedVramGb,
            Boolean mixedPrecisionReady,
            Boolean distributedReady,
            Boolean resumeEnabled,
            Map<String, Object> hyperparameters,
            String experimentName,
            String experimentDescription) {
    }

    /** Request to start scheduling a validated training job. */
    public record StartTrainingRequest(@NotNull UUID jobId) {
    }

    /** Request to cancel a queued or scheduled job. */
    public record CancelTrainingRequest(@NotNull UUID jobId, String reason) {
    }

    /** Request to restore from a checkpoint. */
    public record RestoreCheckpointRequest(@NotNull UUID checkpointId, String reason) {
    }

    /** Training job response. */
    public record TrainingJobResponse(UUID id, String jobName, String baseModel, String modelFamily, String trainingMethod, String status, Integer priority, Integer requestedGpuCount) {
    }

    /** Training run response. */
    public record TrainingRunResponse(UUID id, UUID jobId, String status, String schedulerDecision) {
    }

    /** Experiment response. */
    public record ExperimentResponse(UUID id, String name, String status) {
    }

    /** Model registry response. */
    public record ModelResponse(UUID id, String modelName, String modelFamily, String status) {
    }

    /** Checkpoint response. */
    public record CheckpointResponse(UUID id, UUID jobId, String validationStatus, Boolean restorable) {
    }

    /** Operation response for cancel and restore workflows. */
    public record TrainingOperationResponse(UUID id, String operation, String status, String details, Instant timestamp) {
    }
}
