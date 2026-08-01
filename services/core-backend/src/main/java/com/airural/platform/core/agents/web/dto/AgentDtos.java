/*
 * Purpose: Defines REST contracts for the multi-agent intelligence platform.
 * Why it exists: Clients need stable DTOs for chat, execution, registry, tasks, memory, history, feedback, and tools.
 * Architecture fit: Web adapter contracts for Milestone 9 Multi-Agent Intelligence.
 */
package com.airural.platform.core.agents.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

/** Namespace for agent API DTO records. */
public final class AgentDtos {
    private AgentDtos() {}

    /** Agent chat request. */
    public record AgentChatRequest(@NotBlank String message, UUID conversationId, Map<String, Object> context, Boolean requireHumanApproval) {}

    /** Explicit agent execution request. */
    public record AgentExecuteRequest(@NotBlank String objective, List<String> preferredAgents, Map<String, Object> context, Boolean parallel, Boolean requireHumanApproval) {}

    /** Agent execution response. */
    public record AgentExecutionResponse(UUID executionId, UUID conversationId, UUID planId, String status, String response, List<AgentTaskResponse> tasks, List<ReasoningTraceResponse> reasoning, List<Citation> citations, Double confidence, Boolean requiresApproval, Long latencyMs) {}

    /** Agent registry response. */
    public record AgentResponse(UUID id, String agentKey, String name, String agentType, String status, String description, List<String> capabilities) {}

    /** Agent task response. */
    public record AgentTaskResponse(UUID id, String agentKey, String taskType, String status, String resultJson) {}

    /** Agent memory response. */
    public record AgentMemoryResponse(UUID id, String memoryType, String scopeType, UUID conversationId, String contentJson, Instant createdAt) {}

    /** Conversation history response. */
    public record AgentHistoryResponse(UUID id, UUID conversationId, String status, Double confidence, Boolean requiresApproval, Long latencyMs, Instant createdAt) {}

    /** Feedback request. */
    public record AgentFeedbackRequest(@NotNull UUID executionId, @Min(1) @Max(5) Integer rating, String comment, String approvalDecision) {}

    /** Feedback response. */
    public record AgentFeedbackResponse(UUID id, UUID executionId, Integer rating, String approvalDecision) {}

    /** Tool definition response. */
    public record ToolResponse(UUID id, String toolKey, String name, String category, String description, String healthStatus, String metadataJson) {}

    /** Reasoning trace response. */
    public record ReasoningTraceResponse(UUID id, String agentKey, String content, Double confidence) {}

    /** Citation response. */
    public record Citation(String sourceType, String sourceId, String excerpt, Double score) {}
}
