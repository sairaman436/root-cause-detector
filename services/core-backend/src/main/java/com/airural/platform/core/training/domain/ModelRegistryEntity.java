/*
 * Purpose: Stores governed model registry metadata for training outputs and future deployments.
 * Why it exists: Parent model, base model, adapter, merged model, GGUF, Ollama, vLLM, and license metadata require a durable registry.
 * Architecture fit: AI-3 model registry entity; it records metadata only and does not deploy models.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Training model registry entity. */
@Entity
@Table(name = "model_registry", schema = "training")
public class ModelRegistryEntity {
    @Id private UUID id;
    private UUID jobId;
    private String modelName;
    private String modelFamily;
    private String baseModel;
    private String parentModel;
    private String modelType;
    private String license;
    private String ggufMetadata;
    private String ollamaManifest;
    private String vllmMetadata;
    private String status;
    private Instant createdAt;

    protected ModelRegistryEntity() {}

    /** Creates model registry metadata. */
    public ModelRegistryEntity(UUID id, UUID jobId, String modelName, String modelFamily, String baseModel, String parentModel, String modelType, String license, String ggufMetadata, String ollamaManifest, String vllmMetadata, String status, Instant createdAt) {
        this.id = id; this.jobId = jobId; this.modelName = modelName; this.modelFamily = modelFamily; this.baseModel = baseModel; this.parentModel = parentModel; this.modelType = modelType; this.license = license; this.ggufMetadata = ggufMetadata; this.ollamaManifest = ollamaManifest; this.vllmMetadata = vllmMetadata; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getModelName() { return modelName; }
    public String getModelFamily() { return modelFamily; }
    public String getStatus() { return status; }
}
