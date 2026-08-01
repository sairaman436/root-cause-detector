/*
 * Purpose: Stores production-ready model deployment metadata for serving.
 * Why it exists: No model should receive traffic unless provider, target, quality gate, warmup, rollback, and release status are tracked.
 * Architecture fit: Model serving deployment registry for AI-8.
 */
package com.airural.platform.core.serving.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Model deployment entity. */
@Entity
@Table(name = "model_deployments", schema = "serving")
public class ModelDeploymentEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private String modelKey;
    private String modelVersion;
    private String assistantType;
    private String providerType;
    private String deploymentTarget;
    private String qualityGateStatus;
    private String trafficStatus;
    private Boolean warmupComplete;
    private String rollbackVersion;
    private Instant createdAt;

    protected ModelDeploymentEntity() {}

    /** Creates a model deployment. */
    public ModelDeploymentEntity(UUID id, UUID optimizationRunId, String modelKey, String modelVersion, String assistantType, String providerType, String deploymentTarget, String qualityGateStatus, String trafficStatus, Boolean warmupComplete, String rollbackVersion, Instant createdAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.modelKey = modelKey; this.modelVersion = modelVersion; this.assistantType = assistantType; this.providerType = providerType; this.deploymentTarget = deploymentTarget; this.qualityGateStatus = qualityGateStatus; this.trafficStatus = trafficStatus; this.warmupComplete = warmupComplete; this.rollbackVersion = rollbackVersion; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getModelKey() { return modelKey; }
    public String getProviderType() { return providerType; }
}
