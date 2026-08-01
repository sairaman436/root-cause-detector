/*
 * Purpose: Stores supervised fine-tuning run lifecycle records.
 * Why it exists: AI-4 must track model selection, dataset lineage, training strategy, review state, and completion status for the first Rural Intelligence Foundation Model adapter.
 * Architecture fit: Primary operational entity for the fine-tuning execution module.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Fine-tuning run entity. */
@Entity
@Table(name = "fine_tuning_runs", schema = "finetuning")
public class FineTuningRunEntity {
    @Id private UUID id;
    private String runName;
    private String selectedBaseModel;
    private String selectedModelFamily;
    private String trainingStrategy;
    private String status;
    private String datasetSourceType;
    private UUID datasetId;
    @Column(columnDefinition = "TEXT")
    private String lineageJson;
    @Column(columnDefinition = "TEXT")
    private String benchmarkReportJson;
    private String reviewStatus;
    private Instant startedAt;
    private Instant completedAt;
    private Instant updatedAt;

    protected FineTuningRunEntity() {}

    /** Creates a fine-tuning run record. */
    public FineTuningRunEntity(UUID id, String runName, String selectedBaseModel, String selectedModelFamily, String trainingStrategy, String status, String datasetSourceType, UUID datasetId, String lineageJson, String benchmarkReportJson, String reviewStatus, Instant startedAt, Instant completedAt, Instant updatedAt) {
        this.id = id; this.runName = runName; this.selectedBaseModel = selectedBaseModel; this.selectedModelFamily = selectedModelFamily; this.trainingStrategy = trainingStrategy; this.status = status; this.datasetSourceType = datasetSourceType; this.datasetId = datasetId; this.lineageJson = lineageJson; this.benchmarkReportJson = benchmarkReportJson; this.reviewStatus = reviewStatus; this.startedAt = startedAt; this.completedAt = completedAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getRunName() { return runName; }
    public String getSelectedBaseModel() { return selectedBaseModel; }
    public String getSelectedModelFamily() { return selectedModelFamily; }
    public String getTrainingStrategy() { return trainingStrategy; }
    public String getStatus() { return status; }
    public String getReviewStatus() { return reviewStatus; }
    public void markRolledBack() { this.status = "ROLLED_BACK"; this.reviewStatus = "ROLLBACK_APPROVED"; this.updatedAt = Instant.now(); }
}
