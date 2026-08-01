/*
 * Purpose: Persists top-level agent orchestration executions.
 * Why it exists: Agent latency, confidence, approval, status, and response history must be auditable.
 * Architecture fit: Orchestrator execution aggregate for multi-agent workflows.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for agent executions. */
@Entity
@Table(name = "agent_executions", schema = "agents")
public class AgentExecutionEntity {
    @Id private UUID id;
    private UUID conversationId;
    @Column(nullable = false) private UUID planId;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false, columnDefinition = "TEXT") private String inputJson;
    @Column(nullable = false, columnDefinition = "TEXT") private String outputJson;
    @Column(nullable = false) private Double confidence;
    @Column(nullable = false) private Long latencyMs;
    @Column(nullable = false) private Boolean requiresApproval;
    @Column(nullable = false) private Instant createdAt;
    private Instant completedAt;

    protected AgentExecutionEntity() {}

    public AgentExecutionEntity(UUID conversationId, UUID planId, String inputJson) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.planId = planId;
        this.status = "RUNNING";
        this.inputJson = inputJson;
        this.outputJson = "{}";
        this.confidence = 0.0;
        this.latencyMs = 0L;
        this.requiresApproval = false;
        this.createdAt = Instant.now();
    }

    public void complete(String outputJson, double confidence, long latencyMs, boolean requiresApproval) {
        this.status = "SUCCEEDED";
        this.outputJson = outputJson;
        this.confidence = confidence;
        this.latencyMs = latencyMs;
        this.requiresApproval = requiresApproval;
        this.completedAt = Instant.now();
    }

    public void fail(String outputJson, long latencyMs) {
        this.status = "FAILED";
        this.outputJson = outputJson;
        this.latencyMs = latencyMs;
        this.completedAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID conversationId() { return conversationId; }
    public UUID planId() { return planId; }
    public String status() { return status; }
    public String outputJson() { return outputJson; }
    public Double confidence() { return confidence; }
    public Long latencyMs() { return latencyMs; }
    public Boolean requiresApproval() { return requiresApproval; }
    public Instant createdAt() { return createdAt; }
}
