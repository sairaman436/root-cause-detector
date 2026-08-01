/*
 * Purpose: Defines public dataset engineering API contracts.
 * Why it exists: Dataset clients need stable request and response shapes independent of persistence internals.
 * Architecture fit: REST DTO boundary for the AI-1 Dataset Engineering Platform.
 */
package com.airural.platform.core.datasets.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** DTO namespace for dataset engineering APIs. */
public final class DatasetDtos {
    private DatasetDtos() {
    }

    /** Request to create a governed dataset registry record. */
    public record CreateDatasetRequest(
            @NotBlank String name,
            @NotBlank String type,
            String description,
            List<String> tags,
            Map<String, Object> metadata) {
    }

    /** Dataset registry response. */
    public record DatasetResponse(
            UUID id,
            String name,
            String type,
            String status,
            BigDecimal qualityScore,
            BigDecimal syntheticRatio,
            Instant createdAt) {
    }

    /** Request for dataset pipeline operations. */
    public record DatasetOperationRequest(
            @NotNull UUID datasetId,
            List<DatasetSampleRequest> samples,
            String format,
            String purpose,
            @Min(1) @Max(10000) Integer count) {
    }

    /** Sample submitted for cleaning, validation, or synthetic seed workflows. */
    public record DatasetSampleRequest(
            String sampleType,
            @NotBlank String inputText,
            String outputText,
            String language) {
    }

    /** Response for dataset processing operations. */
    public record DatasetOperationResponse(
            UUID datasetId,
            String operation,
            String status,
            BigDecimal score,
            String findings,
            String artifactUri) {
    }

    /** Latest dataset quality response. */
    public record DatasetQualityResponse(UUID datasetId, BigDecimal qualityScore, String findings) {
    }
}
