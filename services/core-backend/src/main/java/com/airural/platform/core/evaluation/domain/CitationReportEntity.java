/*
 * Purpose: Stores citation verification results.
 * Why it exists: AI-5 must verify citation existence, source match, relevance, broken references, and unsupported claims.
 * Architecture fit: Citation accuracy entity for evaluation gates.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Citation report entity. */
@Entity
@Table(name = "citation_reports", schema = "evaluation")
public class CitationReportEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private BigDecimal citationAccuracy;
    private Integer brokenCitationCount;
    private Integer unsupportedCitationCount;
    @Column(columnDefinition = "TEXT")
    private String findingsJson;
    private Instant createdAt;

    protected CitationReportEntity() {}

    /** Creates a citation report. */
    public CitationReportEntity(UUID id, UUID evaluationRunId, BigDecimal citationAccuracy, Integer brokenCitationCount, Integer unsupportedCitationCount, String findingsJson, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.citationAccuracy = citationAccuracy; this.brokenCitationCount = brokenCitationCount; this.unsupportedCitationCount = unsupportedCitationCount; this.findingsJson = findingsJson; this.createdAt = createdAt;
    }
}
