/*
 * Purpose: JPA entity for aggregate pilot run metrics.
 * Why it exists: Calculated after all scenario results are stored; provides the summary view for the dashboard.
 * Architecture fit: Evaluation bounded context.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Aggregate metrics for one completed pilot run. */
@Entity
@Table(schema = "evaluation", name = "pilot_run_metrics")
public class PilotRunMetricsEntity {

    @Id
    private UUID id;

    @Column(name = "pilot_run_id", nullable = false, unique = true)
    private UUID pilotRunId;

    @Column(name = "root_cause_accuracy")             private BigDecimal rootCauseAccuracy;
    @Column(name = "evidence_groundedness")           private BigDecimal evidenceGroundedness;
    @Column(name = "citation_accuracy")               private BigDecimal citationAccuracy;
    @Column(name = "contradiction_detection_rate")    private BigDecimal contradictionDetectionRate;
    @Column(name = "missing_evidence_detection_rate") private BigDecimal missingEvidenceDetectionRate;
    @Column(name = "unsupported_claim_rate")          private BigDecimal unsupportedClaimRate;
    @Column(name = "hallucination_rate")              private BigDecimal hallucinationRate;
    @Column(name = "recommendation_relevance")        private BigDecimal recommendationRelevance;
    @Column(name = "recommendation_acceptance_rate")  private BigDecimal recommendationAcceptanceRate;
    @Column(name = "human_agreement_rate")            private BigDecimal humanAgreementRate;
    @Column(name = "average_latency_ms")              private BigDecimal averageLatencyMs;
    @Column(name = "failure_rate")                    private BigDecimal failureRate;
    @Column(name = "notes_json", columnDefinition = "jsonb")
    private String notesJson;
    @Column(name = "computed_at", nullable = false)   private Instant computedAt;

    protected PilotRunMetricsEntity() {}

    public PilotRunMetricsEntity(UUID id, UUID pilotRunId) {
        this.id = id;
        this.pilotRunId = pilotRunId;
        this.notesJson = "{}";
        this.computedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPilotRunId() { return pilotRunId; }
    public BigDecimal getRootCauseAccuracy() { return rootCauseAccuracy; }
    public void setRootCauseAccuracy(BigDecimal v) { this.rootCauseAccuracy = v; }
    public BigDecimal getEvidenceGroundedness() { return evidenceGroundedness; }
    public void setEvidenceGroundedness(BigDecimal v) { this.evidenceGroundedness = v; }
    public BigDecimal getCitationAccuracy() { return citationAccuracy; }
    public void setCitationAccuracy(BigDecimal v) { this.citationAccuracy = v; }
    public BigDecimal getContradictionDetectionRate() { return contradictionDetectionRate; }
    public void setContradictionDetectionRate(BigDecimal v) { this.contradictionDetectionRate = v; }
    public BigDecimal getMissingEvidenceDetectionRate() { return missingEvidenceDetectionRate; }
    public void setMissingEvidenceDetectionRate(BigDecimal v) { this.missingEvidenceDetectionRate = v; }
    public BigDecimal getUnsupportedClaimRate() { return unsupportedClaimRate; }
    public void setUnsupportedClaimRate(BigDecimal v) { this.unsupportedClaimRate = v; }
    public BigDecimal getHallucinationRate() { return hallucinationRate; }
    public void setHallucinationRate(BigDecimal v) { this.hallucinationRate = v; }
    public BigDecimal getRecommendationRelevance() { return recommendationRelevance; }
    public void setRecommendationRelevance(BigDecimal v) { this.recommendationRelevance = v; }
    public BigDecimal getRecommendationAcceptanceRate() { return recommendationAcceptanceRate; }
    public void setRecommendationAcceptanceRate(BigDecimal v) { this.recommendationAcceptanceRate = v; }
    public BigDecimal getHumanAgreementRate() { return humanAgreementRate; }
    public void setHumanAgreementRate(BigDecimal v) { this.humanAgreementRate = v; }
    public BigDecimal getAverageLatencyMs() { return averageLatencyMs; }
    public void setAverageLatencyMs(BigDecimal v) { this.averageLatencyMs = v; }
    public BigDecimal getFailureRate() { return failureRate; }
    public void setFailureRate(BigDecimal v) { this.failureRate = v; }
    public String getNotesJson() { return notesJson; }
    public void setNotesJson(String notesJson) { this.notesJson = notesJson; }
    public Instant getComputedAt() { return computedAt; }
}
