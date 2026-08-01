/*
 * Purpose: Persists reasoning steps, evidence graph, policies, models, prompts, agents, and confidence evolution.
 * Why it exists: Decision intelligence requires end-to-end explainability and auditability.
 * Architecture fit: Decision trace record for the explainability framework.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for decision traces. */
@Entity
@Table(name = "decision_traces", schema = "decision")
public class DecisionTraceEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID decisionId;
    @Column(nullable = false) private Integer stepNumber;
    @Column(nullable = false, length = 120) private String stepName;
    @Column(nullable = false, columnDefinition = "TEXT") private String detailsJson;
    @Column(nullable = false) private Double confidenceAfterStep;
    @Column(nullable = false) private Instant createdAt;

    protected DecisionTraceEntity() {}

    public DecisionTraceEntity(UUID decisionId, Integer stepNumber, String stepName, String detailsJson, Double confidenceAfterStep) {
        this.id = UUID.randomUUID();
        this.decisionId = decisionId;
        this.stepNumber = stepNumber;
        this.stepName = stepName;
        this.detailsJson = detailsJson;
        this.confidenceAfterStep = confidenceAfterStep;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String stepName() { return stepName; }
    public String detailsJson() { return detailsJson; }
    public Double confidenceAfterStep() { return confidenceAfterStep; }
}
