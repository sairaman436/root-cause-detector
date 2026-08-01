/*
 * Purpose: Persists MCP-style tool metadata.
 * Why it exists: Agents must discover and invoke internal capabilities only through governed tool adapters.
 * Architecture fit: Tool registry record for the Model Context Protocol inspired layer.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for tool definitions. */
@Entity
@Table(name = "tool_definitions", schema = "agents")
public class ToolDefinitionEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 120) private String toolKey;
    @Column(nullable = false, length = 160) private String name;
    @Column(nullable = false, length = 80) private String category;
    @Column(length = 500) private String description;
    @Column(nullable = false, columnDefinition = "TEXT") private String permissionsJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String metadataJson;
    @Column(nullable = false, length = 40) private String healthStatus;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    protected ToolDefinitionEntity() {}

    public ToolDefinitionEntity(String toolKey, String name, String category, String description, String permissionsJson, String metadataJson) {
        this.id = UUID.randomUUID();
        this.toolKey = toolKey;
        this.name = name;
        this.category = category;
        this.description = description;
        this.permissionsJson = permissionsJson;
        this.metadataJson = metadataJson;
        this.healthStatus = "HEALTHY";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public String toolKey() { return toolKey; }
    public String name() { return name; }
    public String category() { return category; }
    public String description() { return description; }
    public String permissionsJson() { return permissionsJson; }
    public String metadataJson() { return metadataJson; }
    public String healthStatus() { return healthStatus; }
}
