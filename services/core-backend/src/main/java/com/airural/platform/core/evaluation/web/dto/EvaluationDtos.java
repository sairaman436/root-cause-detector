/*
 * Purpose: Defines public enterprise evaluation API contracts.
 * Why it exists: AI-5 clients need stable request and response shapes for runs, results, benchmarks, safety, comparison, promotion, and rejection.
 * Architecture fit: REST DTO boundary for the independent AI evaluation platform.
 */
package com.airural.platform.core.evaluation.web.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** DTO namespace for AI-5 evaluation APIs. */
public final class EvaluationDtos {
    private EvaluationDtos() {
    }

    /** Request to run an immutable model evaluation. */
    public record RunEvaluationRequest(
            @NotNull UUID modelRunId,
            String evaluationType,
            List<String> benchmarkSuites,
            Boolean includeRedTeam,
            Boolean includeExternalAudit) {
    }

    /** Request to promote a model based on an evaluation result. */
    public record PromotionRequest(@NotNull UUID evaluationRunId, String rationale) {
    }

    /** Request to reject a model based on an evaluation result. */
    public record RejectionRequest(@NotNull UUID evaluationRunId, String rationale) {
    }

    /** Evaluation run response. */
    public record EvaluationRunResponse(UUID id, UUID modelRunId, String modelName, String modelFamily, String status, String recommendation, BigDecimal overallScore) {
    }

    /** Benchmark suite response. */
    public record BenchmarkSuiteResponse(UUID id, String name, String category, String status) {
    }

    /** Safety summary response. */
    public record SafetySummaryResponse(UUID evaluationRunId, String status, BigDecimal riskScore, String summary) {
    }

    /** Comparison response. */
    public record ComparisonResponse(UUID evaluationRunId, String recommendation, String summary) {
    }

    /** Promotion or rejection operation response. */
    public record EvaluationDecisionResponse(UUID evaluationRunId, String decision, String status, String details) {
    }
}
