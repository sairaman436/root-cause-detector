/*
 * Purpose: Defines public fine-tuning API contracts.
 * Why it exists: AI-4 clients need stable request and response shapes for starting fine-tuning, listing jobs/models/reports, and rollback.
 * Architecture fit: REST DTO boundary for the supervised fine-tuning lifecycle module.
 */
package com.airural.platform.core.finetuning.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** DTO namespace for AI-4 fine-tuning APIs. */
public final class FineTuningDtos {
    private FineTuningDtos() {
    }

    /** Request to execute a governed fine-tuning lifecycle. */
    public record StartFineTuningRequest(
            @NotBlank String runName,
            @NotBlank String datasetSourceType,
            @NotNull UUID datasetId,
            List<String> candidateModels,
            List<String> trainingTasks,
            Map<String, Object> hyperparameters,
            Boolean requireExternalAudit) {
    }

    /** Request to rollback a fine-tuned adapter release candidate. */
    public record RollbackRequest(@NotNull UUID runId, String reason) {
    }

    /** Fine-tuning job response. */
    public record FineTuningRunResponse(UUID id, String runName, String selectedBaseModel, String selectedModelFamily, String trainingStrategy, String status, String reviewStatus) {
    }

    /** Fine-tuned model response. */
    public record FineTunedModelResponse(UUID id, String modelName, String baseModel) {
    }

    /** Fine-tuning report response. */
    public record FineTuningReportResponse(UUID id, UUID runId, String reportType) {
    }

    /** Rollback operation response. */
    public record FineTuningOperationResponse(UUID id, String operation, String status, String details) {
    }
}
