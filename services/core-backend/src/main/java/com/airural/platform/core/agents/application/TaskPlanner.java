/*
 * Purpose: Creates explainable task plans for multi-agent work.
 * Why it exists: The orchestrator needs a deterministic planning step before routing work to specialized agents.
 * Architecture fit: Planner component in the approved multi-agent architecture.
 */
package com.airural.platform.core.agents.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Service;

/** Deterministic planner for agent tasks. */
@Service
public class TaskPlanner {
    private final ObjectMapper objectMapper;

    public TaskPlanner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> planAgents(String objective, List<String> preferredAgents) {
        if (preferredAgents != null && !preferredAgents.isEmpty()) {
            return preferredAgents;
        }
        String text = objective.toLowerCase(Locale.ROOT);
        List<String> agents = new ArrayList<>();
        if (text.contains("survey")) agents.add("survey-intelligence-agent");
        if (text.contains("root") || text.contains("cause")) agents.add("root-cause-analysis-agent");
        if (text.contains("recommend")) agents.add("recommendation-agent");
        if (text.contains("policy") || text.contains("scheme")) agents.add("policy-knowledge-agent");
        if (text.contains("trend") || text.contains("compare") || text.contains("anomaly")) agents.add("analytics-agent");
        if (text.contains("report") || text.contains("brief")) agents.add("report-generation-agent");
        if (text.contains("research") || text.contains("similar")) agents.add("research-agent");
        if (text.contains("quality") || text.contains("duplicate") || text.contains("complete")) agents.add("data-quality-agent");
        return agents.isEmpty() ? List.of("research-agent", "policy-knowledge-agent") : agents;
    }

    public String planJson(String objective, List<String> agents, boolean parallel) {
        try {
            return objectMapper.writeValueAsString(Map.of("objective", objective, "agents", agents, "mode", parallel ? "PARALLEL" : "SEQUENTIAL"));
        } catch (Exception ex) {
            return "{}";
        }
    }
}
