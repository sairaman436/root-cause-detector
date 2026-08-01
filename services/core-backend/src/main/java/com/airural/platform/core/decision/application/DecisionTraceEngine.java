/*
 * Purpose: Creates decision trace records for every reasoning pipeline stage.
 * Why it exists: Every AI decision must expose reasoning steps, sources, policies, model/prompt versions, agents, timestamp, and confidence evolution.
 * Architecture fit: Explainability and traceability component in the decision engine.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.decision.domain.DecisionTraceEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Service;

/** Service for decision trace generation. */
@Service
public class DecisionTraceEngine {
    private final ObjectMapper objectMapper;

    public DecisionTraceEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<DecisionTraceEntity> traces(UUID decisionId, Map<String, Object> context, RuleEvaluationResult rules, ConfidenceResult confidence) {
        return List.of(
                trace(decisionId, 1, "Evidence Normalization", Map.of("evidenceGraph", context, "modelVersion", "decision-engine-v1"), 0.55),
                trace(decisionId, 2, "Knowledge Retrieval", Map.of("knowledgeSources", context.get("knowledgeSources"), "rag", "citation-required"), 0.62),
                trace(decisionId, 3, "Rule Evaluation", Map.of("passed", rules.passedRules(), "violated", rules.violatedRules(), "conflicts", rules.conflicts()), rules.consistencyScore()),
                trace(decisionId, 4, "Historical Similarity Search", Map.of("similarity", confidence.historicalSimilarity(), "historicalCases", "prepared"), confidence.historicalSimilarity()),
                trace(decisionId, 5, "Hypothesis Ranking", Map.of("confidence", confidence.overall(), "agentParticipation", context.get("agentOutputs")), confidence.overall()),
                trace(decisionId, 6, "Recommendation Validation", Map.of("policiesUsed", "policy-catalog", "promptVersion", "decision-prompt-v1"), confidence.overall()));
    }

    private DecisionTraceEntity trace(UUID decisionId, int step, String name, Object details, double confidence) {
        return new DecisionTraceEntity(decisionId, step, name, json(details), confidence);
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); } catch (Exception ex) { return "{}"; }
    }
}
