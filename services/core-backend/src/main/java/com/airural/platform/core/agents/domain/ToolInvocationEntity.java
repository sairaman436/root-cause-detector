/*
 * Purpose: Persists tool invocation telemetry.
 * Why it exists: Tool usage, latency, failure, permissions, and retry analysis require durable records.
 * Architecture fit: MCP tool invocation audit for the agent platform.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for tool invocations. */
@Entity
@Table(name = "tool_invocations", schema = "agents")
public class ToolInvocationEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 120) private String toolKey;
    private UUID taskId;
    private UUID executionId;
    @Column(nullable = false, columnDefinition = "TEXT") private String inputJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String outputJson;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false) private Long latencyMs;
    @Column(length = 1000) private String errorMessage;
    @Column(nullable = false) private Instant createdAt;

    protected ToolInvocationEntity() {}

    public ToolInvocationEntity(String toolKey, UUID taskId, UUID executionId, String inputJson, String outputJson, String status, Long latencyMs, String errorMessage) {
        this.id = UUID.randomUUID();
        this.toolKey = toolKey;
        this.taskId = taskId;
        this.executionId = executionId;
        this.inputJson = inputJson;
        this.outputJson = outputJson;
        this.status = status;
        this.latencyMs = latencyMs;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String toolKey() { return toolKey; }
    public String status() { return status; }
    public Long latencyMs() { return latencyMs; }
}
