/*
 * Purpose: Verifies agent evaluation and human approval rules.
 * Why it exists: Consequential recommendations and low-confidence outputs must require human review.
 * Architecture fit: Unit coverage for agent safety and evaluation hooks.
 */
package com.airural.platform.core.agents;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.agents.application.AgentEvaluationEngine;
import com.airural.platform.core.agents.web.dto.AgentDtos.Citation;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for agent evaluation. */
class AgentEvaluationEngineTests {
    private final AgentEvaluationEngine evaluator = new AgentEvaluationEngine();

    @Test
    void requiresApprovalForConsequentialOrUncitedOutput() {
        assertThat(evaluator.humanApprovalRequired(true, 0.9, List.of(new Citation("TOOL", "x", "source", 0.8)))).isTrue();
        assertThat(evaluator.humanApprovalRequired(false, 0.9, List.of())).isTrue();
        assertThat(evaluator.humanApprovalRequired(false, 0.8, List.of(new Citation("TOOL", "x", "source", 0.8)))).isFalse();
    }
}
