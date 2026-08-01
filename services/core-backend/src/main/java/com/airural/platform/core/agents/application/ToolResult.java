/*
 * Purpose: Represents a governed tool invocation result.
 * Why it exists: Agent tools need a standard output shape for aggregation, citations, and failure handling.
 * Architecture fit: Value object for MCP-style tool adapter responses.
 */
package com.airural.platform.core.agents.application;

import java.util.*;

/** Standard tool invocation result. */
public record ToolResult(boolean success, Map<String, Object> data, List<Map<String, Object>> citations, String errorMessage) {
    public static ToolResult success(Map<String, Object> data, List<Map<String, Object>> citations) {
        return new ToolResult(true, data, citations, null);
    }

    public static ToolResult failure(String errorMessage) {
        return new ToolResult(false, Map.of(), List.of(), errorMessage);
    }
}
