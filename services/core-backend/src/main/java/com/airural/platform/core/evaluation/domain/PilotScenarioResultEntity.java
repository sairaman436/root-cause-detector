/*
 * Purpose: JPA entity for per-scenario evaluation results.
 * Why it exists: Each scenario result captures all dimension scores, hallucination counts, latency,
 *   the full pipeline output, and a pass/fail decision for every pipeline mode independently.
 * Architecture fit: Evaluation bounded context.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Result record for one scenario in one pilot run. */
@Entity
@Table(schema = "evaluation", name = "pilot_scenario_results")
public class PilotScenarioResultEntity {

    @Id
    private UUID id;

    @Column(name = "pilot_run_id", nullable = false)
    private UUID pilotRunId;

    @Column(name = "scenario_id", nullable = false)
    private UUID scenarioId;

    // Root cause dimensions
    @Column(name = "problem_understanding_score") private BigDecimal problemUnderstandingScore;
    @Column(name = "fact_extraction_score")        private BigDecimal factExtractionScore;
    @Column(name = "evidence_groundedness_score")  private BigDecimal evidenceGroundednessScore;
    @Column(name = "root_cause_relevance_score")   private BigDecimal rootCauseRelevanceScore;
    @Column(name = "alt_hypothesis_quality_score") private BigDecimal altHypothesisQualityScore;
    @Column(name = "contradiction_detection_score") private BigDecimal contradictionDetectionScore;
    @Column(name = "missing_evidence_detection_score") private BigDecimal missingEvidenceDetectionScore;
    @Column(name = "uncertainty_handling_score")   private BigDecimal uncertaintyHandlingScore;
    @Column(name = "citation_accuracy_score")      private BigDecimal citationAccuracyScore;

    // Recommendation dimensions
    @Column(name = "root_cause_alignment_score")       private BigDecimal rootCauseAlignmentScore;
    @Column(name = "rec_evidence_groundedness_score")  private BigDecimal recEvidenceGroundednessScore;
    @Column(name = "recommendation_relevance_score")   private BigDecimal recommendationRelevanceScore;
    @Column(name = "option_diversity_score")           private BigDecimal optionDiversityScore;
    @Column(name = "feasibility_reasoning_score")      private BigDecimal feasibilityReasoningScore;
    @Column(name = "risk_identification_score")        private BigDecimal riskIdentificationScore;
    @Column(name = "scheme_matching_score")            private BigDecimal schemeMatchingScore;
    @Column(name = "implementation_planning_score")    private BigDecimal implementationPlanningScore;

    // Hallucination counts
    @Column(name = "unsupported_claims_count", nullable = false)        private int unsupportedClaimsCount;
    @Column(name = "false_citations_count", nullable = false)           private int falseCitationsCount;
    @Column(name = "invented_statistics_count", nullable = false)       private int inventedStatisticsCount;
    @Column(name = "invented_schemes_count", nullable = false)          private int inventedSchemesCount;
    @Column(name = "false_eligibility_count", nullable = false)         private int falseEligibilityCount;
    @Column(name = "overconfident_conclusions_count", nullable = false)  private int overconfidentConclusionsCount;

    // Overall
    @Column(name = "overall_score")              private BigDecimal overallScore;
    @Column(name = "pass")                       private Boolean pass;
    @Column(name = "latency_ms")                 private Long latencyMs;
    @Column(name = "pipeline_output_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String pipelineOutputJson;

    @Column(name = "evaluation_classification", nullable = false)
    private String evaluationClassification;

    @Column(name = "review_status", nullable = false)
    private String reviewStatus;
    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected PilotScenarioResultEntity() {}

    public PilotScenarioResultEntity(UUID id, UUID pilotRunId, UUID scenarioId) {
        this.id = id;
        this.pilotRunId = pilotRunId;
        this.scenarioId = scenarioId;
        this.unsupportedClaimsCount = 0;
        this.falseCitationsCount = 0;
        this.inventedStatisticsCount = 0;
        this.inventedSchemesCount = 0;
        this.falseEligibilityCount = 0;
        this.overconfidentConclusionsCount = 0;
        this.pipelineOutputJson = "{}";
        this.evaluationClassification = "DEVELOPMENT_SYNTHETIC";
        this.reviewStatus = "PENDING";
        this.evaluatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPilotRunId() { return pilotRunId; }
    public UUID getScenarioId() { return scenarioId; }

    public BigDecimal getProblemUnderstandingScore() { return problemUnderstandingScore; }
    public void setProblemUnderstandingScore(BigDecimal v) { this.problemUnderstandingScore = v; }
    public BigDecimal getFactExtractionScore() { return factExtractionScore; }
    public void setFactExtractionScore(BigDecimal v) { this.factExtractionScore = v; }
    public BigDecimal getEvidenceGroundednessScore() { return evidenceGroundednessScore; }
    public void setEvidenceGroundednessScore(BigDecimal v) { this.evidenceGroundednessScore = v; }
    public BigDecimal getRootCauseRelevanceScore() { return rootCauseRelevanceScore; }
    public void setRootCauseRelevanceScore(BigDecimal v) { this.rootCauseRelevanceScore = v; }
    public BigDecimal getAltHypothesisQualityScore() { return altHypothesisQualityScore; }
    public void setAltHypothesisQualityScore(BigDecimal v) { this.altHypothesisQualityScore = v; }
    public BigDecimal getContradictionDetectionScore() { return contradictionDetectionScore; }
    public void setContradictionDetectionScore(BigDecimal v) { this.contradictionDetectionScore = v; }
    public BigDecimal getMissingEvidenceDetectionScore() { return missingEvidenceDetectionScore; }
    public void setMissingEvidenceDetectionScore(BigDecimal v) { this.missingEvidenceDetectionScore = v; }
    public BigDecimal getUncertaintyHandlingScore() { return uncertaintyHandlingScore; }
    public void setUncertaintyHandlingScore(BigDecimal v) { this.uncertaintyHandlingScore = v; }
    public BigDecimal getCitationAccuracyScore() { return citationAccuracyScore; }
    public void setCitationAccuracyScore(BigDecimal v) { this.citationAccuracyScore = v; }
    public BigDecimal getRootCauseAlignmentScore() { return rootCauseAlignmentScore; }
    public void setRootCauseAlignmentScore(BigDecimal v) { this.rootCauseAlignmentScore = v; }
    public BigDecimal getRecEvidenceGroundednessScore() { return recEvidenceGroundednessScore; }
    public void setRecEvidenceGroundednessScore(BigDecimal v) { this.recEvidenceGroundednessScore = v; }
    public BigDecimal getRecommendationRelevanceScore() { return recommendationRelevanceScore; }
    public void setRecommendationRelevanceScore(BigDecimal v) { this.recommendationRelevanceScore = v; }
    public BigDecimal getOptionDiversityScore() { return optionDiversityScore; }
    public void setOptionDiversityScore(BigDecimal v) { this.optionDiversityScore = v; }
    public BigDecimal getFeasibilityReasoningScore() { return feasibilityReasoningScore; }
    public void setFeasibilityReasoningScore(BigDecimal v) { this.feasibilityReasoningScore = v; }
    public BigDecimal getRiskIdentificationScore() { return riskIdentificationScore; }
    public void setRiskIdentificationScore(BigDecimal v) { this.riskIdentificationScore = v; }
    public BigDecimal getSchemeMatchingScore() { return schemeMatchingScore; }
    public void setSchemeMatchingScore(BigDecimal v) { this.schemeMatchingScore = v; }
    public BigDecimal getImplementationPlanningScore() { return implementationPlanningScore; }
    public void setImplementationPlanningScore(BigDecimal v) { this.implementationPlanningScore = v; }
    public int getUnsupportedClaimsCount() { return unsupportedClaimsCount; }
    public void setUnsupportedClaimsCount(int v) { this.unsupportedClaimsCount = v; }
    public int getFalseCitationsCount() { return falseCitationsCount; }
    public void setFalseCitationsCount(int v) { this.falseCitationsCount = v; }
    public int getInventedStatisticsCount() { return inventedStatisticsCount; }
    public void setInventedStatisticsCount(int v) { this.inventedStatisticsCount = v; }
    public int getInventedSchemesCount() { return inventedSchemesCount; }
    public void setInventedSchemesCount(int v) { this.inventedSchemesCount = v; }
    public int getFalseEligibilityCount() { return falseEligibilityCount; }
    public void setFalseEligibilityCount(int v) { this.falseEligibilityCount = v; }
    public int getOverconfidentConclusionsCount() { return overconfidentConclusionsCount; }
    public void setOverconfidentConclusionsCount(int v) { this.overconfidentConclusionsCount = v; }
    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal v) { this.overallScore = v; }
    public Boolean getPass() { return pass; }
    public void setPass(Boolean pass) { this.pass = pass; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public String getPipelineOutputJson() { return pipelineOutputJson; }
    public void setPipelineOutputJson(String pipelineOutputJson) { this.pipelineOutputJson = pipelineOutputJson; }
    public String getEvaluationClassification() { return evaluationClassification; }
    public void setEvaluationClassification(String evaluationClassification) { this.evaluationClassification = evaluationClassification; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
}
