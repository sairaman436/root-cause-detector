/*
 * Purpose: Persists individual routed agent tasks.
 * Why it exists: The orchestrator needs per-agent traceability, result sharing, retry state, and failure recovery.
 * Architecture fit: Task record produced by planner and task router.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for agent tasks. */
@Entity
@Table(name = "agent_tasks", schema = "agents")
public class AgentTaskEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID executionId;
    private UUID parentTaskId;
    @Column(nullable = false, length = 120) private String agentKey;
    @Column(nullable = false, length = 80) private String taskType;
    @Column(nullable = false, columnDefinition = "TEXT") private String instructions;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false) private Integer priority;
    @Column(nullable = false, columnDefinition = "TEXT") private String resultJson;
    @Column(length = 1000) private String errorMessage;
    @Column(nullable = false) private Instant createdAt;
    private Instant completedAt;

    protected AgentTaskEntity() {}

    public AgentTaskEntity(UUID executionId, String agentKey, String taskType, String instructions, Integer priority) {
        this.id = UUID.randomUUID();
        this.executionId = executionId;
        this.agentKey = agentKey;
        this.taskType = taskType;
        this.instructions = instructions;
        this.status = "PENDING";
        this.priority = priority;
        this.resultJson = "{}";
        this.createdAt = Instant.now();
    }

    public void complete(String resultJson) { this.status = "SUCCEEDED"; this.resultJson = resultJson; this.completedAt = Instant.now(); }
    public void fail(String errorMessage) { this.status = "FAILED"; this.errorMessage = errorMessage; this.completedAt = Instant.now(); }
    public UUID id() { return id; }
    public String agentKey() { return agentKey; }
    public String taskType() { return taskType; }
    public String instructions() { return instructions; }
    public String status() { return status; }
    public String resultJson() { return resultJson; }
}
