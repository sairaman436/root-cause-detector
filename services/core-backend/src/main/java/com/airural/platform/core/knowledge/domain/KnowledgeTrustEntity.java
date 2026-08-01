/*
 * Purpose: Stores source trust, freshness, coverage, and quality scores.
 * Why it exists: RAG and policy retrieval must prefer authoritative, current, and complete sources.
 * Architecture fit: Quality engine output entity for AI-2 governance.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Knowledge source trust and quality score entity. */
@Entity
@Table(name = "knowledge_trust", schema = "knowledge")
public class KnowledgeTrustEntity {
    @Id private UUID id;
    private UUID sourceId;
    private BigDecimal trustScore;
    private BigDecimal freshnessScore;
    private BigDecimal coverageScore;
    private BigDecimal qualityScore;
    private String rationale;
    private Instant evaluatedAt;

    protected KnowledgeTrustEntity() {}

    /** Creates a trust quality report. */
    public KnowledgeTrustEntity(UUID id, UUID sourceId, BigDecimal trustScore, BigDecimal freshnessScore, BigDecimal coverageScore, BigDecimal qualityScore, String rationale, Instant evaluatedAt) {
        this.id = id; this.sourceId = sourceId; this.trustScore = trustScore; this.freshnessScore = freshnessScore; this.coverageScore = coverageScore; this.qualityScore = qualityScore; this.rationale = rationale; this.evaluatedAt = evaluatedAt;
    }
}
