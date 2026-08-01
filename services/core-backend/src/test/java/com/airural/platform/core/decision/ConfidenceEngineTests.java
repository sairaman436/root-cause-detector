/*
 * Purpose: Verifies decision confidence scoring behavior.
 * Why it exists: Milestone 10 requires confidence tests for evidence completeness, rule consistency, gaps, and follow-ups.
 * Architecture fit: Unit coverage for the confidence engine.
 */
package com.airural.platform.core.decision;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.decision.application.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Unit tests for confidence scoring. */
class ConfidenceEngineTests {
    @Test
    void confidenceIncludesMissingEvidenceAndRuleReasons() {
        ConfidenceResult result = new ConfidenceEngine().score(
                Map.of("problemStatement", "fluoride issue"),
                new RuleEvaluationResult(List.of("mandatory-evidence"), List.of("policy-citation-required"), List.of(), 0.5));
        assertThat(result.overall()).isBetween(0.0, 0.99);
        assertThat(result.reasonCodes()).anyMatch(code -> code.contains("RULE_VIOLATIONS"));
        assertThat(result.missingEvidence()).contains("More structured survey evidence", "Policy or scheme citation");
    }
}
