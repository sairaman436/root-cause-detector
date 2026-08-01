/*
 * Purpose: Persists an agent registry entry.
 * Why it exists: The orchestrator needs governed discovery of specialized agents and their capabilities.
 * Architecture fit: Agent registry aggregate for the multi-agent intelligence platform.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for an agent definition. */
@Entity
@Table(name = "agents", schema = "agents")
public class AgentEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 120) private String agentKey;
    @Column(nullable = false, length = 160) private String name;
    @Column(nullable = false, length = 80) private String agentType;
    @Column(nullable = false, length = 40) private String status;
    @Column(length = 500) private String description;
    @Column(nullable = false, columnDefinition = "TEXT") private String capabilitiesJson;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    protected AgentEntity() {}

    public AgentEntity(String agentKey, String name, String agentType, String status, String description, String capabilitiesJson) {
        this.id = UUID.randomUUID();
        this.agentKey = agentKey;
        this.name = name;
        this.agentType = agentType;
        this.status = status;
        this.description = description;
        this.capabilitiesJson = capabilitiesJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public String agentKey() { return agentKey; }
    public String name() { return name; }
    public String agentType() { return agentType; }
    public String status() { return status; }
    public String description() { return description; }
    public String capabilitiesJson() { return capabilitiesJson; }
}
