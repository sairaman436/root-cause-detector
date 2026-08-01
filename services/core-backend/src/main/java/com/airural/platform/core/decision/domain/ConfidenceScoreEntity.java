/*
 * Purpose: Persists confidence scoring components and reason codes.
 * Why it exists: Decision confidence must show evidence completeness, coverage, consistency, similarity, agreement, and gaps.
 * Architecture fit: Confidence engine output record.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.util.UUID;

/** JPA entity for confidence score details. */
@Entity
@Table(name = "confidence_scores", schema = "decision")
public class ConfidenceScoreEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID decisionId;
    @Column(nullable = false) private Double overallConfidence;
    @Column(nullable = false) private Double evidenceCompleteness;
    @Column(nullable = false) private Double knowledgeCoverage;
    @Column(nullable = false) private Double mlConfidence;
    @Column(nullable = false) private Double ruleConsistency;
    @Column(nullable = false) private Double historicalSimilarity;
    @Column(nullable = false) private Double agentAgreement;
    @Column(nullable = false) private Double contradictoryEvidence;
    @Column(nullable = false, columnDefinition = "TEXT") private String reasonCodesJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String missingEvidenceJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String requiredFollowupsJson;

    protected ConfidenceScoreEntity() {}

    public ConfidenceScoreEntity(UUID decisionId, Double overallConfidence, Double evidenceCompleteness, Double knowledgeCoverage, Double mlConfidence, Double ruleConsistency, Double historicalSimilarity, Double agentAgreement, Double contradictoryEvidence, String reasonCodesJson, String missingEvidenceJson, String requiredFollowupsJson) {
        this.id = UUID.randomUUID();
        this.decisionId = decisionId;
        this.overallConfidence = overallConfidence;
        this.evidenceCompleteness = evidenceCompleteness;
        this.knowledgeCoverage = knowledgeCoverage;
        this.mlConfidence = mlConfidence;
        this.ruleConsistency = ruleConsistency;
        this.historicalSimilarity = historicalSimilarity;
        this.agentAgreement = agentAgreement;
        this.contradictoryEvidence = contradictoryEvidence;
        this.reasonCodesJson = reasonCodesJson;
        this.missingEvidenceJson = missingEvidenceJson;
        this.requiredFollowupsJson = requiredFollowupsJson;
    }

    public UUID id() { return id; }
    public Double overallConfidence() { return overallConfidence; }
    public String reasonCodesJson() { return reasonCodesJson; }
    public String missingEvidenceJson() { return missingEvidenceJson; }
    public String requiredFollowupsJson() { return requiredFollowupsJson; }
}
