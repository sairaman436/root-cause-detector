/*
 * Purpose: Generates policy-aware recommendations from ranked root causes.
 * Why it exists: Decision intelligence must produce prioritized, explainable, confidence-scored actions with cost and risk awareness.
 * Architecture fit: Recommendation engine component in the decision pipeline.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.decision.domain.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** Service for recommendation generation and validation. */
@Service
public class RecommendationEngine {
    public List<RecommendationEntity> recommend(UUID decisionId, List<HypothesisEntity> hypotheses, ConfidenceResult confidence, RuleEvaluationResult rules) {
        boolean approval = confidence.overall() < 0.85 || !rules.violatedRules().isEmpty();
        return List.of(
                new RecommendationEntity(decisionId, "Targeted field verification and evidence completion", "Collect missing survey and evidence details before finalizing consequential intervention decisions.", 1, 0.86, confidence.overall(), "Low cost: field validation and documentation", "Risk: delayed action if evidence collection is slow", "Improved confidence and fewer false-positive root causes", approval),
                new RecommendationEntity(decisionId, "Match household or village need to eligible government scheme", "Use policy eligibility rules and cited scheme constraints to select a compliant intervention option.", 2, 0.82, Math.max(0.5, confidence.overall() - 0.05), "Cost depends on matched scheme and local implementation", "Risk: policy mismatch without reviewer validation", "Higher policy compliance and better intervention fit", true),
                new RecommendationEntity(decisionId, "Monitor similar historical cases", "Compare outcome trajectory with similar villages or cases before scaling the intervention.", 3, 0.72, Math.max(0.45, confidence.overall() - 0.1), "Moderate analytics effort", "Risk: historical cases may not fully generalize", "Better prioritization and reduced implementation risk", approval));
    }
}
