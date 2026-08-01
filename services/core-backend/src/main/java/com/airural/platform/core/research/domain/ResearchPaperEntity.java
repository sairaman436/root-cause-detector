/*
 * Purpose: Stores discovered and authored research papers.
 * Why it exists: The laboratory must track scientific papers, reviews, provenance, trust, and publication state.
 * Architecture fit: Research-1 paper registry entity.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Research paper entity. */
@Entity
@Table(name = "research_papers", schema = "research_lab")
public class ResearchPaperEntity {
    @Id private UUID id;
    private UUID projectId;
    private String title;
    private String authors;
    private String source;
    private String doi;
    private String topic;
    private String reviewStatus;
    private Double trustScore;
    private Instant discoveredAt;

    protected ResearchPaperEntity() {}

    /** Creates a research paper. */
    public ResearchPaperEntity(UUID id, UUID projectId, String title, String authors, String source, String doi, String topic, String reviewStatus, Double trustScore, Instant discoveredAt) {
        this.id = id; this.projectId = projectId; this.title = title; this.authors = authors; this.source = source; this.doi = doi; this.topic = topic; this.reviewStatus = reviewStatus; this.trustScore = trustScore; this.discoveredAt = discoveredAt;
    }
}
