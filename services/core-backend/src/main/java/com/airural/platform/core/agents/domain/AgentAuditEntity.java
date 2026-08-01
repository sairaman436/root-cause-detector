/*
 * Purpose: Persists agent platform audit events.
 * Why it exists: Agent execution, approval, feedback, and tool usage need governance-grade audit trails.
 * Architecture fit: Audit record owned by the multi-agent module.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for agent audit events. */
@Entity
@Table(name = "agent_audit", schema = "agents")
public class AgentAuditEntity {
    @Id private UUID id;
    private UUID actorUserId;
    @Column(nullable = false, length = 120) private String action;
    @Column(nullable = false, length = 80) private String targetType;
    private UUID targetId;
    @Column(nullable = false, length = 40) private String outcome;
    @Column(nullable = false, columnDefinition = "TEXT") private String detailsJson;
    @Column(nullable = false) private Instant createdAt;

    protected AgentAuditEntity() {}

    public AgentAuditEntity(UUID actorUserId, String action, String targetType, UUID targetId, String outcome, String detailsJson) {
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
