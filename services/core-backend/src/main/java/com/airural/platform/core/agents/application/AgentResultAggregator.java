/*
 * Purpose: Aggregates specialized agent results into one user-facing response.
 * Why it exists: Multi-agent execution produces multiple task outputs that must be merged with citations and confidence.
 * Architecture fit: Response aggregator in the multi-agent architecture.
 */
package com.airural.platform.core.agents.application;

import com.airural.platform.core.agents.domain.*;
import com.airural.platform.core.agents.web.dto.AgentDtos.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** Aggregates task and reasoning outputs. */
@Service
public class AgentResultAggregator {
    private final ObjectMapper objectMapper;

    public AgentResultAggregator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AggregatedResult aggregate(List<AgentTaskEntity> tasks, List<ReasoningTraceEntity> traces) {
        List<String> sections = traces.stream().map(ReasoningTraceEntity::content).toList();
        double confidence = traces.stream().mapToDouble(ReasoningTraceEntity::confidence).average().orElse(0.6);
        boolean requiresApproval = traces.stream().anyMatch(trace -> trace.agentKey().contains("root-cause") || trace.agentKey().contains("recommendation")) || confidence < 0.75;
        List<Citation> citations = tasks.stream()
                .flatMap(task -> extractCitations(task.resultJson()).stream())
                .toList();
        return new AggregatedResult(String.join("\n", sections), confidence, requiresApproval, citations);
    }

    private List<Citation> extractCitations(String json) {
        try {
            JsonNode node = objectMapper.readTree(json).path("citations");
            List<Citation> citations = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    citations.add(new Citation(item.path("sourceType").asText("TOOL"), item.path("sourceId").asText("unknown"), item.path("excerpt").asText("Tool citation"), item.path("score").asDouble(0.7)));
                }
            }
            return citations;
        } catch (Exception ex) {
            return List.of();
        }
    }

    /** Aggregated response value. */
    public record AggregatedResult(String response, double confidence, boolean requiresApproval, List<Citation> citations) {}
}
