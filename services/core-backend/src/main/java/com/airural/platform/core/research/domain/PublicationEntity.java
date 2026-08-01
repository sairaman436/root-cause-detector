/*
 * Purpose: Stores laboratory publications.
 * Why it exists: Research papers, technical reports, experiment reports, benchmarks, whitepapers, RFCs, and scientific reviews need review and release tracking.
 * Architecture fit: Research-1 publication system entity.
 */
package com.airural.platform.core.research.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Publication entity. */
@Entity
@Table(name = "publications", schema = "research_lab")
public class PublicationEntity {
    @Id private UUID id;
    private UUID projectId;
    private String publicationType;
    private String title;
    private String abstractText;
    private String authors;
    private String reviewStatus;
    private String uri;
    private Instant publishedAt;

    protected PublicationEntity() {}

    /** Creates a publication. */
    public PublicationEntity(UUID id, UUID projectId, String publicationType, String title, String abstractText, String authors, String reviewStatus, String uri, Instant publishedAt) {
        this.id = id; this.projectId = projectId; this.publicationType = publicationType; this.title = title; this.abstractText = abstractText; this.authors = authors; this.reviewStatus = reviewStatus; this.uri = uri; this.publishedAt = publishedAt;
    }

    public UUID getId() { return id; }
    public String getPublicationType() { return publicationType; }
    public String getTitle() { return title; }
    public String getReviewStatus() { return reviewStatus; }
}
