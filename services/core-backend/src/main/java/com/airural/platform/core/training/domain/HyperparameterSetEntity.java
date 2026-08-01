/*
 * Purpose: Stores immutable hyperparameter set records.
 * Why it exists: Experiment comparison and reproducibility require versioned hyperparameter snapshots.
 * Architecture fit: AI-3 hyperparameter registry entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Hyperparameter set entity. */
@Entity
@Table(name = "hyperparameter_sets", schema = "training")
public class HyperparameterSetEntity {
    @Id private UUID id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String parametersJson;
    private String precisionMode;
    private Boolean gradientCheckpointing;
    private Instant createdAt;

    protected HyperparameterSetEntity() {}

    /** Creates a hyperparameter set. */
    public HyperparameterSetEntity(UUID id, String name, String parametersJson, String precisionMode, Boolean gradientCheckpointing, Instant createdAt) {
        this.id = id; this.name = name; this.parametersJson = parametersJson; this.precisionMode = precisionMode; this.gradientCheckpointing = gradientCheckpointing; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
}
