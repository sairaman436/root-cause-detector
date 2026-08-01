/*
 * Purpose: Defines REST contracts for the AI-8 serving gateway.
 * Why it exists: Clients need stable inference, stream, health, metrics, model, and session payloads.
 * Architecture fit: DTO boundary for enterprise model serving APIs.
 */
package com.airural.platform.core.serving.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

/** Container for serving DTO records. */
public final class ServingDtos {
    private ServingDtos() {}

    /** Request for unified inference gateway. */
    public record ServingInferenceRequest(@NotBlank String prompt, String taskType, String assistantType, String language, String userRole, UUID sessionId, Map<String, Object> context, Boolean stream, Boolean batch, Boolean async, String requestSignature, String tenantId) {}

    /** Serving inference response. */
    public record ServingInferenceResponse(UUID inferenceId, UUID sessionId, String selectedModel, String provider, String response, Integer promptTokens, Integer completionTokens, Long latencyMs, boolean fallbackUsed, boolean cacheHit, String routingPolicy, String status) {}

    /** Serving model response. */
    public record ServingModelResponse(UUID id, String modelKey, String providerType, String trafficStatus) {}

    /** Serving health response. */
    public record ServingHealthResponse(String status, Integer activeNodes, Integer healthyNodes, String circuitBreakerStatus) {}

    /** Serving metrics response. */
    public record ServingMetricsResponse(BigDecimal requestsPerSecond, BigDecimal p95LatencyMs, BigDecimal tokensPerSecond, BigDecimal errorRate, BigDecimal timeoutRate) {}

    /** Serving session response. */
    public record ServingSessionResponse(UUID id, UUID userId, String status) {}
}
