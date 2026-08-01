/*
 * Purpose: Defines the standard MCP-style tool interface for agents.
 * Why it exists: Agents must invoke platform capabilities through adapters rather than direct service calls.
 * Architecture fit: Tool contract for the Model Context Protocol inspired layer.
 */
package com.airural.platform.core.agents.application;

import java.util.Map;

/** Standard tool adapter contract. */
public interface AgentTool {
    String key();
    String category();
    ToolResult invoke(Map<String, Object> input);
}
