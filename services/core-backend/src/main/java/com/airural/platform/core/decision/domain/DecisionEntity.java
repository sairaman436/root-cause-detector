/*
 * Purpose: Persists a decision intelligence analysis.
 * Why it exists: Root-cause decisions and recommendations must be durable, explainable, auditable, and reviewable.
 * Architecture fit: Aggregate root for the Decision Intelligence Engine.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for decisions. */
@Entity
@Table(name = "decisions", schema = "decision")
public class DecisionEntity {
    @Id private UUID id;
    private UUID surveyId;
    private UUID organizationId;
    private UUID requestedBy;
    @Column(nullable = false, length = 80) private String decisionType;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false, columnDefinition = "TEXT") private String inputJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String finalDecision;
    @Column(nullable = false) private Double overallConfidence;
    @Column(nullable = false) private Boolean humanApprovalRequired;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    protected DecisionEntity() {}

    public DecisionEntity(UUID surveyId, UUID organizationId, UUID requestedBy, String decisionType, String inputJson) {
        this.id = UUID.randomUUID();
        this.surveyId = surveyId;
        this.organizationId = organizationId;
        this.requestedBy = requestedBy;
        this.decisionType = decisionType;
        this.status = "IN_PROGRESS";
        this.inputJson = inputJson;
        this.finalDecision = "";
        this.overallConfidence = 0.0;
        this.humanApprovalRequired = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void complete(String finalDecision, double confidence, boolean humanApprovalRequired) {
        this.status = "COMPLETED";
        this.finalDecision = finalDecision;
        this.overallConfidence = confidence;
        this.humanApprovalRequired = humanApprovalRequired;
        this.updatedAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID surveyId() { return surveyId; }
    public UUID organizationId() { return organizationId; }
    public String decisionType() { return decisionType; }
    public String status() { return status; }
    public String finalDecision() { return finalDecision; }
    public Double overallConfidence() { return overallConfidence; }
    public Boolean humanApprovalRequired() { return humanApprovalRequired; }
    public Instant createdAt() { return createdAt; }
}
