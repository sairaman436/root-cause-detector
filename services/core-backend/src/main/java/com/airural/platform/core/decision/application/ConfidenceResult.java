/*
 * Purpose: Represents confidence engine output.
 * Why it exists: Decisions need confidence components, reason codes, missing evidence, and follow-up requirements.
 * Architecture fit: Value object produced by the confidence engine and persisted by the decision service.
 */
package com.airural.platform.core.decision.application;

import java.util.*;

/** Confidence score output. */
public record ConfidenceResult(double overall, double evidenceCompleteness, double knowledgeCoverage, double mlConfidence, double ruleConsistency, double historicalSimilarity, double agentAgreement, double contradictoryEvidence, List<String> reasonCodes, List<String> missingEvidence, List<String> requiredFollowups) {}
