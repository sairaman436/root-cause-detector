/*
 * Purpose: JPA entity for a single synthetic evaluation scenario.
 * Why it exists: Every scenario must carry its expected outputs, village context, domain, and adversarial flag
 *   so the evaluation engine can execute it and score results against expectations.
 * Architecture fit: Evaluation bounded context. All data is SYNTHETIC — no real village statistics.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A single SYNTHETIC scenario in a pilot evaluation dataset. */
@Entity
@Table(schema = "evaluation", name = "pilot_scenarios")
public class PilotScenarioEntity {

    @Id
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "scenario_id", nullable = false)
    private String scenarioId;

    @Column(name = "synthetic_label", nullable = false)
    private String syntheticLabel;

    @Column(name = "village_context", nullable = false, columnDefinition = "TEXT")
    private String villageContext;

    @Column(nullable = false)
    private String domain;

    @Column(name = "problem_statement", nullable = false, columnDefinition = "TEXT")
    private String problemStatement;

    @Column(name = "survey_data_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String surveyDataJson;

    @Column(name = "evidence_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String evidenceJson;

    @Column(name = "knowledge_documents_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String knowledgeDocumentsJson;

    @Column(name = "expected_relevant_evidence", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String expectedRelevantEvidenceJson;

    @Column(name = "expected_root_cause_candidates", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String expectedRootCauseCandidatesJson;

    @Column(name = "expected_uncertainties", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String expectedUncertaintiesJson;

    @Column(name = "expected_recommendation_categories", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String expectedRecommendationCategoriesJson;

    @Column(name = "evaluation_classification", nullable = false)
    private String evaluationClassification;

    @Column(name = "scenario_provenance_json", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String scenarioProvenanceJson;

    @Column(name = "review_status", nullable = false)
    private String reviewStatus;

    @Column(name = "evaluation_notes", columnDefinition = "TEXT")
    private String evaluationNotes;

    @Column(name = "adversarial", nullable = false)
    private boolean adversarial;

    @Column(name = "adversarial_type")
    private String adversarialType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PilotScenarioEntity() {}

    public PilotScenarioEntity(
            UUID id, UUID datasetId, String scenarioId, String villageContext,
            String domain, String problemStatement, String surveyDataJson,
            String evidenceJson, String knowledgeDocumentsJson,
            String expectedRelevantEvidenceJson, String expectedRootCauseCandidatesJson,
            String expectedUncertaintiesJson, String expectedRecommendationCategoriesJson,
            String evaluationNotes, boolean adversarial, String adversarialType) {
        this(id, datasetId, scenarioId, villageContext, domain, problemStatement, surveyDataJson, evidenceJson, knowledgeDocumentsJson, expectedRelevantEvidenceJson, expectedRootCauseCandidatesJson, expectedUncertaintiesJson, expectedRecommendationCategoriesJson, evaluationNotes, adversarial, adversarialType, "SYNTHETIC");
    }

    /** Creates a scenario with an explicit provenance label for governed evaluations. */
    public PilotScenarioEntity(
            UUID id, UUID datasetId, String scenarioId, String villageContext,
            String domain, String problemStatement, String surveyDataJson,
            String evidenceJson, String knowledgeDocumentsJson,
            String expectedRelevantEvidenceJson, String expectedRootCauseCandidatesJson,
            String expectedUncertaintiesJson, String expectedRecommendationCategoriesJson,
            String evaluationNotes, boolean adversarial, String adversarialType, String syntheticLabel) {
        this.id = id;
        this.datasetId = datasetId;
        this.scenarioId = scenarioId;
        this.syntheticLabel = syntheticLabel == null || syntheticLabel.isBlank() ? "SYNTHETIC" : syntheticLabel;
        this.villageContext = villageContext;
        this.domain = domain;
        this.problemStatement = problemStatement;
        this.surveyDataJson = surveyDataJson;
        this.evidenceJson = evidenceJson;
        this.knowledgeDocumentsJson = knowledgeDocumentsJson;
        this.expectedRelevantEvidenceJson = expectedRelevantEvidenceJson;
        this.expectedRootCauseCandidatesJson = expectedRootCauseCandidatesJson;
        this.expectedUncertaintiesJson = expectedUncertaintiesJson;
        this.expectedRecommendationCategoriesJson = expectedRecommendationCategoriesJson;
        this.evaluationNotes = evaluationNotes;
        this.adversarial = adversarial;
        this.adversarialType = adversarialType;
        this.evaluationClassification = "DEVELOPMENT_SYNTHETIC";
        this.scenarioProvenanceJson = "{}";
        this.reviewStatus = "PENDING";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getDatasetId() { return datasetId; }
    public String getScenarioId() { return scenarioId; }
    public String getSyntheticLabel() { return syntheticLabel; }
    public String getVillageContext() { return villageContext; }
    public String getDomain() { return domain; }
    public String getProblemStatement() { return problemStatement; }
    public String getSurveyDataJson() { return surveyDataJson; }
    public String getEvidenceJson() { return evidenceJson; }
    public String getKnowledgeDocumentsJson() { return knowledgeDocumentsJson; }
    public String getExpectedRelevantEvidenceJson() { return expectedRelevantEvidenceJson; }
    public String getExpectedRootCauseCandidatesJson() { return expectedRootCauseCandidatesJson; }
    public String getExpectedUncertaintiesJson() { return expectedUncertaintiesJson; }
    public String getExpectedRecommendationCategoriesJson() { return expectedRecommendationCategoriesJson; }
    public String getEvaluationNotes() { return evaluationNotes; }
    public boolean isAdversarial() { return adversarial; }
    public String getAdversarialType() { return adversarialType; }
    public String getEvaluationClassification() { return evaluationClassification; }
    public void setEvaluationClassification(String evaluationClassification) { this.evaluationClassification = evaluationClassification; }
    public String getScenarioProvenanceJson() { return scenarioProvenanceJson; }
    public void setScenarioProvenanceJson(String scenarioProvenanceJson) { this.scenarioProvenanceJson = scenarioProvenanceJson; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public Instant getCreatedAt() { return createdAt; }
}
