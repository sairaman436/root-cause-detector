/*
 * Purpose: Maps specialized agents to their allowed tools.
 * Why it exists: Agents require controlled tool permissions and cannot directly call platform services.
 * Architecture fit: Task router and MCP permission layer.
 */
package com.airural.platform.core.agents.application;

import java.util.*;
import org.springframework.stereotype.Service;

/** Routes agent tasks to MCP tools. */
@Service
public class TaskRouter {
    private static final Map<String, List<String>> ROUTES = Map.of(
            "survey-intelligence-agent", List.of("survey.service", "rag.service"),
            "root-cause-analysis-agent", List.of("evidence.service", "knowledge.service", "rag.service", "ai.foundation"),
            "recommendation-agent", List.of("knowledge.service", "analytics.service", "rag.service"),
            "policy-knowledge-agent", List.of("knowledge.service", "rag.service"),
            "analytics-agent", List.of("analytics.service", "geography.service", "survey.service"),
            "report-generation-agent", List.of("survey.service", "evidence.service", "geography.service", "ai.foundation"),
            "research-agent", List.of("knowledge.service", "survey.service", "rag.service"),
            "data-quality-agent", List.of("survey.service", "evidence.service", "analytics.service"));

    public List<String> toolsFor(String agentKey) {
        return ROUTES.getOrDefault(agentKey, List.of("rag.service", "ai.foundation"));
    }
}
