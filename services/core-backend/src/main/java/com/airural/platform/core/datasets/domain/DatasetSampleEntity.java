/*
 * Purpose: Stores individual dataset samples and validation status.
 * Why it exists: Dataset quality, annotation, deduplication, and export require sample-level governance.
 * Architecture fit: Sample registry for the AI-1 dataset pipeline.
 */
package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Dataset sample entity. */
@Entity
@Table(name = "dataset_samples", schema = "datasets")
public class DatasetSampleEntity {
    @Id private UUID id;
    private UUID datasetId;
    private UUID versionId;
    private String sampleType;
    @Column(columnDefinition = "TEXT")
    private String inputText;
    @Column(columnDefinition = "TEXT")
    private String outputText;
    private String language;
    private String fingerprint;
    private Boolean synthetic;
    private String validationStatus;
    private Instant createdAt;

    protected DatasetSampleEntity() {}

    public DatasetSampleEntity(UUID id, UUID datasetId, UUID versionId, String sampleType, String inputText, String outputText, String language, String fingerprint, Boolean synthetic, String validationStatus, Instant createdAt) {
        this.id = id; this.datasetId = datasetId; this.versionId = versionId; this.sampleType = sampleType; this.inputText = inputText; this.outputText = outputText; this.language = language; this.fingerprint = fingerprint; this.synthetic = synthetic; this.validationStatus = validationStatus; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getDatasetId() { return datasetId; }
    public UUID getVersionId() { return versionId; }
    public String getSampleType() { return sampleType; }
    public String getInputText() { return inputText; }
    public String getOutputText() { return outputText; }
    public String getLanguage() { return language; }
    public String getFingerprint() { return fingerprint; }
    public Boolean getSynthetic() { return synthetic; }
    public String getValidationStatus() { return validationStatus; }
    public Instant getCreatedAt() { return createdAt; }
}
