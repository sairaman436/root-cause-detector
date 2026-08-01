/*
 * Purpose: Persists execution state for a task plan.
 * Why it exists: Retry, timeout, cancellation, and recovery need durable execution state.
 * Architecture fit: Execution control record for the agent orchestrator.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for task execution state. */
@Entity
@Table(name = "task_executions", schema = "agents")
public class TaskExecutionEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID planId;
    @Column(nullable = false, length = 40) private String status;
    @Column(nullable = false) private Integer retryCount;
    @Column(length = 1000) private String failureReason;
    @Column(nullable = false) private Instant startedAt;
    private Instant completedAt;

    protected TaskExecutionEntity() {}

    public TaskExecutionEntity(UUID planId) {
        this.id = UUID.randomUUID();
        this.planId = planId;
        this.status = "RUNNING";
        this.retryCount = 0;
        this.startedAt = Instant.now();
    }

    public void complete() { this.status = "SUCCEEDED"; this.completedAt = Instant.now(); }
    public void fail(String reason) { this.status = "FAILED"; this.failureReason = reason; this.completedAt = Instant.now(); }
    public UUID id() { return id; }
    public UUID planId() { return planId; }
    public String status() { return status; }
}
