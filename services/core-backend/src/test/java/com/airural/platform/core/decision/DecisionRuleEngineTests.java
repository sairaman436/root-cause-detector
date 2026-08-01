/*
 * Purpose: Verifies configurable rule evaluation.
 * Why it exists: Milestone 10 requires rule engine tests for policy rules, conflicts, and mandatory conditions.
 * Architecture fit: Unit coverage for the decision rule engine.
 */
package com.airural.platform.core.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.airural.platform.core.decision.application.DecisionRuleEngine;
import com.airural.platform.core.decision.domain.DecisionRuleEntity;
import com.airural.platform.core.decision.infrastructure.DecisionRuleRepository;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Unit tests for rule evaluation. */
class DecisionRuleEngineTests {
    @Test
    void detectsPolicyAndConflictRuleViolations() {
        DecisionRuleRepository repository = mock(DecisionRuleRepository.class);
        when(repository.findByStatusOrderByPriorityAsc("ACTIVE")).thenReturn(List.of(
                new DecisionRuleEntity("mandatory-evidence", "Mandatory", "MANDATORY_CONDITION", "dsl", 1, "ACTIVE"),
                new DecisionRuleEntity("policy-citation-required", "Policy", "POLICY_RULE", "dsl", 2, "ACTIVE"),
                new DecisionRuleEntity("conflict-detection", "Conflict", "CONFLICT_DETECTION", "dsl", 3, "ACTIVE")));
        var result = new DecisionRuleEngine(repository).evaluate(Map.of("surveyEvidence", Map.of("issue", "conflict")));
        assertThat(result.passedRules()).contains("mandatory-evidence");
        assertThat(result.violatedRules()).contains("policy-citation-required", "conflict-detection");
        assertThat(result.conflicts()).contains("conflict-detection");
    }
}
