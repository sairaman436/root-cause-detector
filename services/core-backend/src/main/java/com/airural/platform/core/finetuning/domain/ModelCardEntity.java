/*
 * Purpose: Stores model card metadata for the fine-tuned adapter.
 * Why it exists: Release review requires intended use, limitations, license, safety notes, and structured model metadata.
 * Architecture fit: Governance artifact entity for AI-4 model outputs.
 */
package com.airural.platform.core.finetuning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Model card entity. */
@Entity
@Table(name = "model_cards", schema = "finetuning")
public class ModelCardEntity {
    @Id private UUID id;
    private UUID runId;
    private String modelName;
    private String baseModel;
    @Column(columnDefinition = "TEXT")
    private String intendedUse;
    @Column(columnDefinition = "TEXT")
    private String limitations;
    private String license;
    @Column(columnDefinition = "TEXT")
    private String safetyNotes;
    @Column(columnDefinition = "TEXT")
    private String cardJson;
    private Instant createdAt;

    protected ModelCardEntity() {}

    /** Creates a model card. */
    public ModelCardEntity(UUID id, UUID runId, String modelName, String baseModel, String intendedUse, String limitations, String license, String safetyNotes, String cardJson, Instant createdAt) {
        this.id = id; this.runId = runId; this.modelName = modelName; this.baseModel = baseModel; this.intendedUse = intendedUse; this.limitations = limitations; this.license = license; this.safetyNotes = safetyNotes; this.cardJson = cardJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getModelName() { return modelName; }
    public String getBaseModel() { return baseModel; }
}
