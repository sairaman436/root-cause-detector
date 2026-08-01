/*
 * Purpose: Calculates confidence for decision intelligence outputs.
 * Why it exists: Trustworthy decisions require explicit scoring from evidence completeness, coverage, rules, similarity, agent agreement, and contradictions.
 * Architecture fit: Confidence engine component in the reasoning pipeline.
 */
package com.airural.platform.core.decision.application;

import java.util.*;
import org.springframework.stereotype.Service;

/** Service for confidence scoring. */
@Service
public class ConfidenceEngine {
    /** Scores the analysis context. */
    public ConfidenceResult score(Map<String, Object> context, RuleEvaluationResult rules) {
        double evidence = context.containsKey("surveyEvidence") ? 0.85 : 0.55;
        double knowledge = context.toString().toLowerCase(Locale.ROOT).contains("policy") ? 0.82 : 0.62;
        double ml = Optional.ofNullable(context.get("mlConfidence")).filter(Number.class::isInstance).map(Number.class::cast).map(Number::doubleValue).orElse(0.7);
        double historical = context.toString().toLowerCase(Locale.ROOT).contains("historical") ? 0.78 : 0.6;
        double agent = context.containsKey("agentOutputs") ? 0.8 : 0.62;
        double contradiction = rules.conflicts().isEmpty() ? 0.0 : 0.25;
        double overall = clamp(((evidence + knowledge + ml + rules.consistencyScore() + historical + agent) / 6.0) - contradiction);
        List<String> reasons = new ArrayList<>(List.of("EVIDENCE_COMPLETENESS", "KNOWLEDGE_COVERAGE", "RULE_CONSISTENCY", "HISTORICAL_SIMILARITY", "AGENT_AGREEMENT"));
        if (!rules.violatedRules().isEmpty()) {
            reasons.add("RULE_VIOLATIONS:" + String.join(",", rules.violatedRules()));
        }
        List<String> missing = new ArrayList<>();
        if (evidence < 0.7) missing.add("More structured survey evidence");
        if (knowledge < 0.7) missing.add("Policy or scheme citation");
        List<String> followups = missing.isEmpty() ? List.of("Human reviewer approval before consequential action") : missing.stream().map(item -> "Collect " + item).toList();
        return new ConfidenceResult(overall, evidence, knowledge, ml, rules.consistencyScore(), historical, agent, contradiction, reasons, missing, followups);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(0.99, value));
    }
}
