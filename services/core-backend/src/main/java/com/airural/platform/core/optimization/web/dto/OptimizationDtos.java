/*
 * Purpose: Defines REST contracts for the AI-6 optimization platform.
 * Why it exists: Clients need stable request and response models for optimization jobs, artifacts, benchmarks, packages, and release promotion.
 * Architecture fit: DTO boundary for optimization REST APIs.
 */
package com.airural.platform.core.optimization.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Container for optimization DTO records. */
public final class OptimizationDtos {
    private OptimizationDtos() {}

    /** Request to start optimization packaging from a passed evaluation run. */
    public record StartOptimizationRequest(@NotNull UUID evaluationRunId, List<String> exportFormats, List<String> deploymentTargets, Boolean includeAdapterMerge, Boolean includeAdapterSeparation, String releaseNotes) {}

    /** Request to promote an optimization release candidate. */
    public record PromoteOptimizationRequest(@NotNull UUID optimizationRunId, String promotedBy, String rationale) {}

    /** Response for an optimization run. */
    public record OptimizationRunResponse(UUID id, UUID evaluationRunId, UUID modelRunId, String modelName, String modelFamily, String status, String releaseRecommendation) {}

    /** Response for optimized artifacts. */
    public record ArtifactResponse(UUID id, String exportFormat, String checksumSha256, String validationStatus) {}

    /** Response for deployment packages. */
    public record PackageResponse(UUID id, String packageType, String targetEnvironment, String status) {}

    /** Benchmark summary response. */
    public record BenchmarkSummaryResponse(UUID optimizationRunId, BigDecimal firstTokenLatencyMs, BigDecimal tokensPerSecond, BigDecimal peakMemoryGb, Integer concurrentRequests, String status) {}

    /** Promotion response. */
    public record OptimizationDecisionResponse(UUID optimizationRunId, String decision, String status, String details) {}
}
