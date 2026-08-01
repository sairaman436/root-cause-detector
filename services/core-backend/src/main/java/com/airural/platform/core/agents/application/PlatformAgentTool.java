/*
 * Purpose: Implements deterministic platform tool adapters.
 * Why it exists: Milestone 9 requires MCP-style tool boundaries without external API integrations or direct agent-service coupling.
 * Architecture fit: Tool adapter used by specialized agents through the tool registry.
 */
package com.airural.platform.core.agents.application;

import java.util.*;

/** Generic platform tool adapter for current and future service surfaces. */
public class PlatformAgentTool implements AgentTool {
    private final String key;
    private final String category;

    public PlatformAgentTool(String key, String category) {
        this.key = key;
        this.category = category;
    }

    @Override public String key() { return key; }
    @Override public String category() { return category; }

    @Override
    public ToolResult invoke(Map<String, Object> input) {
        String objective = String.valueOf(input.getOrDefault("objective", ""));
        Map<String, Object> data = Map.of(
                "toolKey", key,
                "category", category,
                "summary", "Tool adapter prepared context for: " + objective,
                "requiresExternalIntegration", key.startsWith("future."));
        List<Map<String, Object>> citations = List.of(Map.of(
                "sourceType", "TOOL",
                "sourceId", key,
                "excerpt", "MCP tool adapter response for " + category,
                "score", 0.8));
        return ToolResult.success(data, citations);
    }
}
