/*
 * Purpose: Stores research datasets used by laboratory projects and experiments.
 * Why it exists: New datasets require provenance, license, quality, governance, and reproducibility metadata.
 * Architecture fit: Research-1 dataset registry entity separate from production dataset engineering.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research dataset entity. */
@Entity
@Table(name = "research_datasets", schema = "research_lab")
public class ResearchDatasetEntity {
    @Id private UUID id;
    private UUID projectId;
    private String datasetKey;
    private String title;
    private String source;
    private String license;
    private Double qualityScore;
    private String governanceStatus;
    private Instant registeredAt;

    protected ResearchDatasetEntity() {}

    /** Creates a research dataset. */
    public ResearchDatasetEntity(UUID id, UUID projectId, String datasetKey, String title, String source, String license, Double qualityScore, String governanceStatus, Instant registeredAt) {
        this.id = id; this.projectId = projectId; this.datasetKey = datasetKey; this.title = title; this.source = source; this.license = license; this.qualityScore = qualityScore; this.governanceStatus = governanceStatus; this.registeredAt = registeredAt;
    }
}
