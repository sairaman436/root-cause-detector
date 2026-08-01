/*
 * Purpose: Stores requested model training jobs.
 * Why it exists: The training factory must validate datasets, model family, method, priority, and resource requirements before scheduling.
 * Architecture fit: AI-3 training job manager entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Training job entity. */
@Entity
@Table(name = "training_jobs", schema = "training")
public class TrainingJobEntity {
    @Id private UUID id;
    private UUID experimentId;
    private UUID hyperparameterSetId;
    private String jobName;
    private String baseModel;
    private String modelFamily;
    private String trainingMethod;
    private String datasetSourceType;
    private UUID datasetId;
    private String status;
    private Integer priority;
    private Integer requestedGpuCount;
    private Integer requestedVramGb;
    private Boolean mixedPrecisionReady;
    private Boolean distributedReady;
    private Boolean resumeEnabled;
    @Column(columnDefinition = "TEXT")
    private String lineageJson;
    private Instant createdAt;
    private Instant updatedAt;

    protected TrainingJobEntity() {}

    /** Creates a validated training job. */
    public TrainingJobEntity(UUID id, UUID experimentId, UUID hyperparameterSetId, String jobName, String baseModel, String modelFamily, String trainingMethod, String datasetSourceType, UUID datasetId, String status, Integer priority, Integer requestedGpuCount, Integer requestedVramGb, Boolean mixedPrecisionReady, Boolean distributedReady, Boolean resumeEnabled, String lineageJson, Instant createdAt, Instant updatedAt) {
        this.id = id; this.experimentId = experimentId; this.hyperparameterSetId = hyperparameterSetId; this.jobName = jobName; this.baseModel = baseModel; this.modelFamily = modelFamily; this.trainingMethod = trainingMethod; this.datasetSourceType = datasetSourceType; this.datasetId = datasetId; this.status = status; this.priority = priority; this.requestedGpuCount = requestedGpuCount; this.requestedVramGb = requestedVramGb; this.mixedPrecisionReady = mixedPrecisionReady; this.distributedReady = distributedReady; this.resumeEnabled = resumeEnabled; this.lineageJson = lineageJson; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getJobName() { return jobName; }
    public String getBaseModel() { return baseModel; }
    public String getModelFamily() { return modelFamily; }
    public String getTrainingMethod() { return trainingMethod; }
    public String getStatus() { return status; }
    public Integer getPriority() { return priority; }
    public Integer getRequestedGpuCount() { return requestedGpuCount; }
    public Integer getRequestedVramGb() { return requestedVramGb; }
    public void markQueued() { this.status = "QUEUED"; this.updatedAt = Instant.now(); }
    public void markScheduled() { this.status = "SCHEDULED"; this.updatedAt = Instant.now(); }
    public void cancel() { this.status = "CANCELLED"; this.updatedAt = Instant.now(); }
}
