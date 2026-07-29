/*
 * Purpose: Persists model version metadata and runtime requirements.
 * Why it exists: The platform must route and approve models by concrete version, capability, and resource need.
 * Architecture fit: Child entity of the AI model registry.
 */
package com.airural.platform.core.ai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for a model version. */
@Entity
@Table(name = "model_versions", schema = "ai")
public class ModelVersionEntity {
    @Id
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private AIModelEntity model;
    @Column(nullable = false, length = 80)
    private String versionName;
    @Column(nullable = false, length = 80)
    private String parameterCount;
    @Column(length = 80)
    private String quantization;
    @Column(length = 180)
    private String licenseName;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String capabilities;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String supportedLanguages;
    @Column(length = 80)
    private String memoryRequirement;
    @Column(length = 80)
    private String gpuRequirement;
    @Column(nullable = false)
    private Integer contextLength;
    @Column(nullable = false)
    private Boolean embeddingSupport;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(nullable = false)
    private Instant createdAt;

    protected ModelVersionEntity() {}

    public ModelVersionEntity(AIModelEntity model, String versionName, String parameterCount, String quantization, String licenseName, String capabilities, String supportedLanguages, String memoryRequirement, String gpuRequirement, Integer contextLength, Boolean embeddingSupport, String status) {
        this.id = UUID.randomUUID();
        this.model = model;
        this.versionName = versionName;
        this.parameterCount = parameterCount;
        this.quantization = quantization;
        this.licenseName = licenseName;
        this.capabilities = capabilities;
        this.supportedLanguages = supportedLanguages;
        this.memoryRequirement = memoryRequirement;
        this.gpuRequirement = gpuRequirement;
        this.contextLength = contextLength;
        this.embeddingSupport = embeddingSupport;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public AIModelEntity model() { return model; }
    public String versionName() { return versionName; }
    public String parameterCount() { return parameterCount; }
    public String quantization() { return quantization; }
    public String licenseName() { return licenseName; }
    public String capabilities() { return capabilities; }
    public String supportedLanguages() { return supportedLanguages; }
    public String memoryRequirement() { return memoryRequirement; }
    public String gpuRequirement() { return gpuRequirement; }
    public Integer contextLength() { return contextLength; }
    public Boolean embeddingSupport() { return embeddingSupport; }
    public String status() { return status; }
}
