/*
 * Purpose: Stores governed AI agent definitions.
 * Why it exists: Agent autonomy, allowed tools, escalation rules, and human approval requirements must be controlled before execution.
 * Architecture fit: Agent governance registry for AI-9.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Agent registry entity. */
@Entity
@Table(name = "agent_registry", schema = "governance")
public class AgentRegistryEntity {
    @Id private UUID id;
    private String agentKey;
    private String version;
    private String ownerRole;
    private String maximumAutonomyLevel;
    private String allowedTools;
    private String escalationRules;
    private String humanApprovalRules;
    private String status;
    private Instant createdAt;

    protected AgentRegistryEntity() {}

    /** Creates an agent registry entry. */
    public AgentRegistryEntity(UUID id, String agentKey, String version, String ownerRole, String maximumAutonomyLevel, String allowedTools, String escalationRules, String humanApprovalRules, String status, Instant createdAt) {
        this.id = id; this.agentKey = agentKey; this.version = version; this.ownerRole = ownerRole; this.maximumAutonomyLevel = maximumAutonomyLevel; this.allowedTools = allowedTools; this.escalationRules = escalationRules; this.humanApprovalRules = humanApprovalRules; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getAgentKey() { return agentKey; }
    public String getMaximumAutonomyLevel() { return maximumAutonomyLevel; }
    public String getStatus() { return status; }
}
