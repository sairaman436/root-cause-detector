/*
 * Purpose: Represents rule engine evaluation output.
 * Why it exists: Confidence and recommendation validators need rule pass/fail, conflicts, and reason codes.
 * Architecture fit: Value object shared by decision rule and confidence engines.
 */
package com.airural.platform.core.decision.application;

import java.util.*;

/** Rule evaluation result. */
public record RuleEvaluationResult(List<String> passedRules, List<String> violatedRules, List<String> conflicts, double consistencyScore) {}
