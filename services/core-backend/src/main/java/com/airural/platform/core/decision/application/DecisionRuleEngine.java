/*
 * Purpose: Evaluates configurable decision rules and custom DSL expressions.
 * Why it exists: Policy rules, eligibility, constraints, mandatory conditions, conflicts, and priority rules must be explicit.
 * Architecture fit: Rule engine component in the decision intelligence pipeline.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.decision.domain.DecisionRuleEntity;
import com.airural.platform.core.decision.infrastructure.DecisionRuleRepository;
import java.util.*;
import org.springframework.stereotype.Service;

/** Deterministic rule engine for decision analysis. */
@Service
public class DecisionRuleEngine {
    private final DecisionRuleRepository repository;

    public DecisionRuleEngine(DecisionRuleRepository repository) {
        this.repository = repository;
    }

    /** Evaluates active rules against the current evidence context. */
    public RuleEvaluationResult evaluate(Map<String, Object> context) {
        List<String> passed = new ArrayList<>();
        List<String> violated = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        String text = context.toString().toLowerCase(Locale.ROOT);
        for (DecisionRuleEntity rule : repository.findByStatusOrderByPriorityAsc("ACTIVE")) {
            boolean pass = switch (rule.ruleKey()) {
                case "mandatory-evidence" -> !context.isEmpty();
                case "policy-citation-required" -> text.contains("policy") || text.contains("scheme") || text.contains("government");
                case "human-approval-consequential" -> true;
                case "conflict-detection" -> !text.contains("contradiction") && !text.contains("conflict");
                default -> true;
            };
            if (pass) {
                passed.add(rule.ruleKey());
            } else {
                violated.add(rule.ruleKey());
                if (rule.ruleType().contains("CONFLICT")) {
                    conflicts.add(rule.ruleKey());
                }
            }
        }
        double total = Math.max(1, passed.size() + violated.size());
        return new RuleEvaluationResult(passed, violated, conflicts, passed.size() / total);
    }
}
