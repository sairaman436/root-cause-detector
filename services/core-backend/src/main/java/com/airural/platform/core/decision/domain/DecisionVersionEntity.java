/*
 * Purpose: Persists immutable versions of a decision output.
 * Why it exists: Review, overrides, and re-analysis require version history.
 * Architecture fit: Version child record for decision memory.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for decision versions. */
@Entity
@Table(name = "decision_versions", schema = "decision")
public class DecisionVersionEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID decisionId;
    @Column(nullable = false) private Integer versionNumber;
    @Column(nullable = false, columnDefinition = "TEXT") private String decisionJson;
    @Column(nullable = false) private Double confidence;
    @Column(nullable = false) private Instant createdAt;

    protected DecisionVersionEntity() {}

    public DecisionVersionEntity(UUID decisionId, Integer versionNumber, String decisionJson, Double confidence) {
        this.id = UUID.randomUUID();
        this.decisionId = decisionId;
        this.versionNumber = versionNumber;
        this.decisionJson = decisionJson;
        this.confidence = confidence;
        this.createdAt = Instant.now();
    }
}
