/*
 * Purpose: Generates primary and alternative root-cause hypotheses.
 * Why it exists: Decision intelligence must compare explanations before ranking a final root cause.
 * Architecture fit: Hypothesis generation component in the root-cause discovery pipeline.
 */
package com.airural.platform.core.decision.application;

import com.airural.platform.core.decision.domain.HypothesisEntity;
import java.util.*;
import org.springframework.stereotype.Service;

/** Service for hypothesis generation. */
@Service
public class HypothesisGenerator {
    public List<HypothesisEntity> generate(UUID decisionId, Map<String, Object> context, ConfidenceResult confidence) {
        String problem = String.valueOf(context.getOrDefault("problemStatement", "rural service gap"));
        return List.of(
                new HypothesisEntity(decisionId, "Primary systemic service gap", "Survey, evidence, policy, and agent context indicate the likely root cause is a systemic service delivery gap related to " + problem, confidence.overall(), 1, false),
                new HypothesisEntity(decisionId, "Resource availability constraint", "Alternative explanation: local resource, staffing, or infrastructure constraints may be contributing factors.", Math.max(0.2, confidence.overall() - 0.12), 2, true),
                new HypothesisEntity(decisionId, "Data completeness limitation", "Alternative explanation: missing or contradictory evidence may be biasing the observed pattern.", Math.max(0.2, confidence.overall() - 0.2), 3, true));
    }
}
