/*
 * Purpose: Scores source trust, freshness, metadata completeness, and corpus coverage.
 * Why it exists: Enterprise RAG and policy retrieval need quality gates before documents become retrieval-ready.
 * Architecture fit: Deterministic quality engine for AI-2 knowledge acquisition.
 */
package com.airural.platform.core.knowledge.application;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Deterministic knowledge quality scoring engine. */
@Component
public class KnowledgeQualityEngine {
    /** Computes a conservative source trust score from the trust tier. */
    public BigDecimal trustScore(String trustTier) {
        return switch (trustTier == null ? "" : trustTier.toUpperCase()) {
            case "PRIMARY_GOVERNMENT", "MULTILATERAL" -> BigDecimal.valueOf(0.98);
            case "RESEARCH", "NGO_VALIDATED" -> BigDecimal.valueOf(0.90);
            default -> BigDecimal.valueOf(0.75);
        };
    }

    /** Computes a quality score from duplicate status and metadata completeness. */
    public BigDecimal qualityScore(boolean duplicate, boolean completeMetadata, BigDecimal trustScore) {
        if (duplicate) {
            return BigDecimal.valueOf(0.30);
        }
        BigDecimal metadata = completeMetadata ? BigDecimal.valueOf(0.02) : BigDecimal.valueOf(-0.10);
        BigDecimal score = trustScore.add(metadata);
        return score.min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }
}
