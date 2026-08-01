/*
 * Purpose: Stores experiment registry records for model training research.
 * Why it exists: Experiments need governed metadata, comparison groups, and ownership before training jobs are scheduled.
 * Architecture fit: AI-3 experiment tracking entity.
 */
package com.airural.platform.core.training.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Experiment registry entity. */
@Entity
@Table(name = "training_experiments", schema = "training")
public class TrainingExperimentEntity {
    @Id private UUID id;
    private String name;
    private String description;
    private String ownerTeam;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String metadataJson;
    private Instant createdAt;
    private Instant updatedAt;

    protected TrainingExperimentEntity() {}

    /** Creates an experiment registry record. */
    public TrainingExperimentEntity(UUID id, String name, String description, String ownerTeam, String status, String metadataJson, Instant createdAt, Instant updatedAt) {
        this.id = id; this.name = name; this.description = description; this.ownerTeam = ownerTeam; this.status = status; this.metadataJson = metadataJson; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}
