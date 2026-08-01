/*
 * Purpose: Stores scientific findings produced by experiments and reviews.
 * Why it exists: Research discoveries need evidence, confidence, replication state, and publication linkage.
 * Architecture fit: Research-1 finding registry entity.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research finding entity. */
@Entity
@Table(name = "research_findings", schema = "research_lab")
public class ResearchFindingEntity {
    @Id private UUID id;
    private UUID projectId;
    private UUID experimentId;
    private String title;
    private String summary;
    private String evidenceRef;
    private Double confidence;
    private String replicationStatus;
    private Instant createdAt;

    protected ResearchFindingEntity() {}

    /** Creates a research finding. */
    public ResearchFindingEntity(UUID id, UUID projectId, UUID experimentId, String title, String summary, String evidenceRef, Double confidence, String replicationStatus, Instant createdAt) {
        this.id = id; this.projectId = projectId; this.experimentId = experimentId; this.title = title; this.summary = summary; this.evidenceRef = evidenceRef; this.confidence = confidence; this.replicationStatus = replicationStatus; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public Double getConfidence() { return confidence; }
    public String getReplicationStatus() { return replicationStatus; }
}
