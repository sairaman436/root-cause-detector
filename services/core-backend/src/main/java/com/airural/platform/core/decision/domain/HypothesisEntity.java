/*
 * Purpose: Persists generated and alternative hypotheses.
 * Why it exists: Explainable root-cause discovery requires alternatives, ranking, and evidence support.
 * Architecture fit: Hypothesis record owned by the reasoning engine.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.util.UUID;

/** JPA entity for root-cause hypotheses. */
@Entity
@Table(name = "hypotheses", schema = "decision")
public class HypothesisEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID decisionId;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String rationale;
    @Column(nullable = false) private Double confidence;
    @Column(nullable = false) private Integer rank;
    @Column(nullable = false) private Boolean alternative;

    protected HypothesisEntity() {}

    public HypothesisEntity(UUID decisionId, String title, String rationale, Double confidence, Integer rank, Boolean alternative) {
        this.id = UUID.randomUUID();
        this.decisionId = decisionId;
        this.title = title;
        this.rationale = rationale;
        this.confidence = confidence;
        this.rank = rank;
        this.alternative = alternative;
    }

    public UUID id() { return id; }
    public String title() { return title; }
    public String rationale() { return rationale; }
    public Double confidence() { return confidence; }
    public Integer rank() { return rank; }
    public Boolean alternative() { return alternative; }
}
