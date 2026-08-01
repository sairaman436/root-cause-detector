/*
 * Purpose: Binds governance policies to AI agents and tool permissions.
 * Why it exists: Agent behavior must respect policy boundaries independently from prompt and model governance.
 * Architecture fit: Join entity for AI-9 agent policy enforcement.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Agent policy entity. */
@Entity
@Table(name = "agent_policies", schema = "governance")
public class AgentPolicyEntity {
    @Id private UUID id;
    private UUID agentId;
    private UUID policyId;
    private String toolPermissions;
    private String executionPolicy;
    private String status;
    private Instant createdAt;

    protected AgentPolicyEntity() {}

    /** Creates an agent policy binding. */
    public AgentPolicyEntity(UUID id, UUID agentId, UUID policyId, String toolPermissions, String executionPolicy, String status, Instant createdAt) {
        this.id = id; this.agentId = agentId; this.policyId = policyId; this.toolPermissions = toolPermissions; this.executionPolicy = executionPolicy; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
}
