/*
 * Purpose: Stores hallucination and unsupported-claim findings.
 * Why it exists: AI-5 must quantify hallucination rate, missing evidence, and unsupported claims before promotion.
 * Architecture fit: Evaluation report entity for factuality gates.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Hallucination report entity. */
@Entity
@Table(name = "hallucination_reports", schema = "evaluation")
public class HallucinationReportEntity {
    @Id private UUID id;
    private UUID evaluationRunId;
    private BigDecimal hallucinationRate;
    private Integer unsupportedClaimCount;
    @Column(columnDefinition = "TEXT")
    private String findingsJson;
    private Instant createdAt;

    protected HallucinationReportEntity() {}

    /** Creates a hallucination report. */
    public HallucinationReportEntity(UUID id, UUID evaluationRunId, BigDecimal hallucinationRate, Integer unsupportedClaimCount, String findingsJson, Instant createdAt) {
        this.id = id; this.evaluationRunId = evaluationRunId; this.hallucinationRate = hallucinationRate; this.unsupportedClaimCount = unsupportedClaimCount; this.findingsJson = findingsJson; this.createdAt = createdAt;
    }
}
