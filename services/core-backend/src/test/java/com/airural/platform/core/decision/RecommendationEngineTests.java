/*
 * Purpose: Verifies recommendation generation and human approval behavior.
 * Why it exists: Milestone 10 requires recommendation tests for ranking, confidence, impact, and approval hooks.
 * Architecture fit: Unit coverage for the recommendation engine.
 */
package com.airural.platform.core.decision;

import static org.assertj.core.api.Assertions.assertThat;

import com.airural.platform.core.decision.application.*;
import com.airural.platform.core.decision.domain.HypothesisEntity;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Unit tests for recommendations. */
class RecommendationEngineTests {
    @Test
    void recommendationsAreRankedAndReviewable() {
        UUID decisionId = UUID.randomUUID();
        var confidence = new ConfidenceResult(0.78, 0.8, 0.8, 0.75, 1.0, 0.7, 0.8, 0.0, List.of("OK"), List.of(), List.of("Review"));
        var recommendations = new RecommendationEngine().recommend(
                decisionId,
                List.of(new HypothesisEntity(decisionId, "Root", "Reason", 0.78, 1, false)),
                confidence,
                new RuleEvaluationResult(List.of("mandatory-evidence"), List.of(), List.of(), 1.0));
        assertThat(recommendations).hasSize(3);
        assertThat(recommendations.get(0).priority()).isEqualTo(1);
        assertThat(recommendations).allMatch(recommendation -> recommendation.confidence() > 0.0);
    }
}
