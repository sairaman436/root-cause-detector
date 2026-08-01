/*
 * Purpose: Discovers, authorizes, invokes, and logs MCP-style tools.
 * Why it exists: Agents must never directly call internal services; all access goes through governed tool adapters.
 * Architecture fit: Tool registry and invocation layer for the multi-agent platform.
 */
package com.airural.platform.core.agents.application;

import com.airural.platform.core.agents.domain.*;
import com.airural.platform.core.agents.infrastructure.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for tool discovery and invocation. */
@Service
public class AgentToolRegistry {
    private final ToolDefinitionRepository definitionRepository;
    private final ToolInvocationRepository invocationRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, AgentTool> tools;

    public AgentToolRegistry(ToolDefinitionRepository definitionRepository, ToolInvocationRepository invocationRepository, ObjectMapper objectMapper) {
        this.definitionRepository = definitionRepository;
        this.invocationRepository = invocationRepository;
        this.objectMapper = objectMapper;
        this.tools = Map.ofEntries(
                Map.entry("survey.service", new PlatformAgentTool("survey.service", "Survey Service")),
                Map.entry("evidence.service", new PlatformAgentTool("evidence.service", "Evidence Service")),
                Map.entry("knowledge.service", new PlatformAgentTool("knowledge.service", "Knowledge Service")),
                Map.entry("geography.service", new PlatformAgentTool("geography.service", "Geography Service")),
                Map.entry("ai.foundation", new PlatformAgentTool("ai.foundation", "AI Foundation")),
                Map.entry("rag.service", new PlatformAgentTool("rag.service", "RAG Service")),
                Map.entry("analytics.service", new PlatformAgentTool("analytics.service", "Analytics")),
                Map.entry("future.weather.api", new PlatformAgentTool("future.weather.api", "Future Weather API")),
                Map.entry("future.census.api", new PlatformAgentTool("future.census.api", "Future Census API")),
                Map.entry("future.satellite.api", new PlatformAgentTool("future.satellite.api", "Future Satellite API")));
    }

    @Transactional(readOnly = true)
    public List<ToolDefinitionEntity> definitions() {
        return definitionRepository.findAll();
    }

    @Transactional
    public ToolResult invoke(String toolKey, UUID taskId, UUID executionId, Map<String, Object> input) {
        Instant started = Instant.now();
        AgentTool tool = Optional.ofNullable(tools.get(toolKey))
                .orElseThrow(() -> new AgentException("TOOL_NOT_FOUND", "Tool is not registered: " + toolKey, HttpStatus.NOT_FOUND));
        ToolResult result;
        try {
            result = tool.invoke(input);
        } catch (Exception ex) {
            result = ToolResult.failure(ex.getMessage());
        }
        invocationRepository.save(new ToolInvocationEntity(
                toolKey,
                taskId,
                executionId,
                json(input),
                json(result.data()),
                result.success() ? "SUCCEEDED" : "FAILED",
                Duration.between(started, Instant.now()).toMillis(),
                result.errorMessage()));
        return result;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }
}
