/*
 * Purpose: Stores exported and quantized model artifact metadata.
 * Why it exists: Every GGUF, safetensors, ONNX, vLLM, TensorRT-LLM, Ollama, and quantized artifact needs compatibility, checksum, and validation state.
 * Architecture fit: Artifact registry entity under the AI-6 optimization run.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Optimization artifact entity. */
@Entity
@Table(name = "optimization_artifacts", schema = "optimization")
public class OptimizationArtifactEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private UUID profileId;
    private String artifactName;
    private String exportFormat;
    private String quantizationMode;
    private String precisionMode;
    private String storageUri;
    private Long artifactSizeBytes;
    private String checksumSha256;
    private String validationStatus;
    @Column(columnDefinition = "TEXT")
    private String validationJson;
    private Instant createdAt;

    protected OptimizationArtifactEntity() {}

    /** Creates an artifact record. */
    public OptimizationArtifactEntity(UUID id, UUID optimizationRunId, UUID profileId, String artifactName, String exportFormat, String quantizationMode, String precisionMode, String storageUri, Long artifactSizeBytes, String checksumSha256, String validationStatus, String validationJson, Instant createdAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.profileId = profileId; this.artifactName = artifactName; this.exportFormat = exportFormat; this.quantizationMode = quantizationMode; this.precisionMode = precisionMode; this.storageUri = storageUri; this.artifactSizeBytes = artifactSizeBytes; this.checksumSha256 = checksumSha256; this.validationStatus = validationStatus; this.validationJson = validationJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getExportFormat() { return exportFormat; }
    public String getChecksumSha256() { return checksumSha256; }
    public String getValidationStatus() { return validationStatus; }
}
