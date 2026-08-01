/*
 * Purpose: Stores generated governance board reports.
 * Why it exists: Governance, security, compliance, architecture, external audit, and release boards need durable decision evidence.
 * Architecture fit: AI-9 reporting record without implementing analytics dashboards.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Governance report entity. */
@Entity
@Table(name = "governance_reports", schema = "governance")
public class GovernanceReportEntity {
    @Id private UUID id;
    private String reportType;
    private String period;
    private Integer policyViolations;
    private Integer openRisks;
    private Double complianceScore;
    private Double modelTrustScore;
    private Double datasetTrustScore;
    private String summaryJson;
    private Instant generatedAt;

    protected GovernanceReportEntity() {}

    /** Creates a governance report. */
    public GovernanceReportEntity(UUID id, String reportType, String period, Integer policyViolations, Integer openRisks, Double complianceScore, Double modelTrustScore, Double datasetTrustScore, String summaryJson, Instant generatedAt) {
        this.id = id; this.reportType = reportType; this.period = period; this.policyViolations = policyViolations; this.openRisks = openRisks; this.complianceScore = complianceScore; this.modelTrustScore = modelTrustScore; this.datasetTrustScore = datasetTrustScore; this.summaryJson = summaryJson; this.generatedAt = generatedAt;
    }

    public UUID getId() { return id; }
}
