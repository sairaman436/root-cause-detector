/*
 * Purpose: Defines REST contracts for AI foundation operations.
 * Why it exists: Clients need stable DTOs for chat, embeddings, model registry, prompts, RAG, usage, and inference telemetry.
 * Architecture fit: Web adapter contracts for Milestone 8 AI Foundation.
 */
package com.airural.platform.core.ai.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

/** Namespace for AI API DTO records. */
public final class AiDtos {
    private AiDtos() {}

    /** Chat request routed through the AI gateway. */
    public record ChatRequest(@NotBlank String message, String modelId, UUID sessionId, Map<String, Object> context, Boolean stream) {}

    /** Chat response with token and latency telemetry. */
    public record ChatResponse(UUID inferenceId, String modelId, String response, Integer promptTokens, Integer completionTokens, Long latencyMs, boolean fallbackUsed, List<CitationResponse> citations) {}

    /** Embedding request for chunking and vector metadata insertion. */
    public record EmbedRequest(@NotBlank String text, @NotBlank String collectionName, String sourceType, UUID sourceId, String embeddingModel, Map<String, Object> metadata) {}

    /** Embedding response. */
    public record EmbedResponse(UUID jobId, String collectionName, String embeddingModel, Integer chunkCount, Long embeddingTimeMs, String status) {}

    /** Model registration request. */
    public record RegisterModelRequest(@NotBlank String modelId, @NotBlank String name, @NotBlank String version, @NotBlank String family, String parameterCount, String quantization, @NotBlank String provider, String license, String status, List<String> capabilities, List<String> supportedLanguages, String memoryRequirement, String gpuRequirement, Integer contextLength, Boolean embeddingSupport) {}

    /** Model registry response. */
    public record ModelResponse(UUID id, String modelId, String name, String version, String family, String parameterCount, String quantization, String provider, String license, String status, List<String> capabilities, List<String> supportedLanguages, String memoryRequirement, String gpuRequirement, Integer contextLength, Boolean embeddingSupport) {}

    /** Prompt creation request. */
    public record PromptRequest(@NotBlank String name, String category, String description, @NotBlank String templateText, Map<String, Object> variables, String status) {}

    /** Prompt response. */
    public record PromptResponse(UUID id, String name, String category, String status, Integer version, String templateText, Map<String, Object> variables) {}

    /** RAG query request. */
    public record RagQueryRequest(@NotBlank String query, String collectionName, String modelId, UUID sessionId, Map<String, Object> context, Integer topK) {}

    /** RAG query response. */
    public record RagQueryResponse(UUID requestId, String answer, List<CitationResponse> citations, Long retrievalLatencyMs, Long inferenceLatencyMs) {}

    /** Citation response. */
    public record CitationResponse(String sourceType, String sourceId, String excerpt, Double score) {}

    /** Usage response. */
    public record UsageResponse(UUID id, String modelId, Integer totalTokens, Double estimatedCost, Instant createdAt) {}

    /** Inference response. */
    public record InferenceResponse(UUID id, String modelId, String requestType, String status, Integer promptTokens, Integer completionTokens, Long latencyMs, Boolean safetyBlocked, Instant createdAt) {}
}
