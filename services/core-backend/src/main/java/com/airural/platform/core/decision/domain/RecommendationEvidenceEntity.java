/*
 * Purpose: Links recommendations to supporting evidence and citations.
 * Why it exists: Every recommendation must expose survey, document, policy, and historical support.
 * Architecture fit: Explainability join record for recommendation evidence.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.util.UUID;

/** JPA entity for recommendation evidence. */
@Entity
@Table(name = "recommendation_evidence", schema = "decision")
public class RecommendationEvidenceEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID recommendationId;
    @Column(nullable = false, length = 80) private String evidenceType;
    @Column(nullable = false, length = 180) private String evidenceRef;
    @Column(nullable = false, columnDefinition = "TEXT") private String summary;
    @Column(nullable = false) private Double weight;

    protected RecommendationEvidenceEntity() {}

    public RecommendationEvidenceEntity(UUID recommendationId, String evidenceType, String evidenceRef, String summary, Double weight) {
        this.id = UUID.randomUUID();
        this.recommendationId = recommendationId;
        this.evidenceType = evidenceType;
        this.evidenceRef = evidenceRef;
        this.summary = summary;
        this.weight = weight;
    }
}
