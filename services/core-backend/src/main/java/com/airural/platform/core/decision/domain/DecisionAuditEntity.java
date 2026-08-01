/*
 * Purpose: Persists audit events for decision intelligence operations.
 * Why it exists: Decision analysis and review actions require compliance-grade audit logs.
 * Architecture fit: Audit record owned by the decision module.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for decision audit events. */
@Entity
@Table(name = "decision_audit", schema = "decision")
public class DecisionAuditEntity {
    @Id private UUID id;
    private UUID actorUserId;
    @Column(nullable = false, length = 120) private String action;
    @Column(nullable = false, length = 80) private String targetType;
    private UUID targetId;
    @Column(nullable = false, length = 40) private String outcome;
    @Column(nullable = false, columnDefinition = "TEXT") private String detailsJson;
    @Column(nullable = false) private Instant createdAt;

    protected DecisionAuditEntity() {}

    public DecisionAuditEntity(UUID actorUserId, String action, String targetType, UUID targetId, String outcome, String detailsJson) {
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.outcome = outcome;
        this.detailsJson = detailsJson;
        this.createdAt = Instant.now();
    }
}
