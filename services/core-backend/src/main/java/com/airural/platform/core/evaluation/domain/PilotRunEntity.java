/*
 * Purpose: JPA entity for a pilot evaluation run.
 * Why it exists: Each run corresponds to one pipeline mode (BASE_QWEN, QWEN_RAG, FULL_PIPELINE)
 *   so comparison across the three pipeline layers is reproducible.
 * Architecture fit: Evaluation bounded context.
 */
package com.airural.platform.core.evaluation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A single reproducible pilot evaluation run. */
@Entity
@Table(schema = "evaluation", name = "pilot_runs")
public class PilotRunEntity {

    @Id
    private UUID id;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "run_label", nullable = false)
    private String runLabel;

    @Column(name = "pipeline_mode", nullable = false)
    private String pipelineMode;

    @Column(nullable = false)
    private String model;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Column(name = "knowledge_snapshot", nullable = false)
    private String knowledgeSnapshot;

    @Column(nullable = false)
    private String status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "total_scenarios", nullable = false)
    private int totalScenarios;

    @Column(name = "passed_scenarios", nullable = false)
    private int passedScenarios;

    @Column(name = "failed_scenarios", nullable = false)
    private int failedScenarios;

    @Column(name = "run_metadata_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String runMetadataJson;

    @Column(name = "evaluation_classification", nullable = false)
    private String evaluationClassification;

    @Column(name = "review_status", nullable = false)
    private String reviewStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PilotRunEntity() {}

    public PilotRunEntity(UUID id, UUID datasetId, String runLabel, String pipelineMode,
                          String model, String modelVersion, String promptVersion, String knowledgeSnapshot) {
        this.id = id;
        this.datasetId = datasetId;
        this.runLabel = runLabel;
        this.pipelineMode = pipelineMode;
        this.model = model;
        this.modelVersion = modelVersion;
        this.promptVersion = promptVersion;
        this.knowledgeSnapshot = knowledgeSnapshot;
        this.status = "PENDING";
        this.totalScenarios = 0;
        this.passedScenarios = 0;
        this.failedScenarios = 0;
        this.runMetadataJson = "{}";
        this.evaluationClassification = "DEVELOPMENT_SYNTHETIC";
        this.reviewStatus = "PENDING";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getDatasetId() { return datasetId; }
    public String getRunLabel() { return runLabel; }
    public String getPipelineMode() { return pipelineMode; }
    public String getModel() { return model; }
    public String getModelVersion() { return modelVersion; }
    public String getPromptVersion() { return promptVersion; }
    public String getKnowledgeSnapshot() { return knowledgeSnapshot; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public int getTotalScenarios() { return totalScenarios; }
    public void setTotalScenarios(int totalScenarios) { this.totalScenarios = totalScenarios; }
    public int getPassedScenarios() { return passedScenarios; }
    public void setPassedScenarios(int passedScenarios) { this.passedScenarios = passedScenarios; }
    public int getFailedScenarios() { return failedScenarios; }
    public void setFailedScenarios(int failedScenarios) { this.failedScenarios = failedScenarios; }
    public String getRunMetadataJson() { return runMetadataJson; }
    public void setRunMetadataJson(String runMetadataJson) { this.runMetadataJson = runMetadataJson; }
    public String getEvaluationClassification() { return evaluationClassification; }
    public void setEvaluationClassification(String evaluationClassification) { this.evaluationClassification = evaluationClassification; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public Instant getCreatedAt() { return createdAt; }
}
