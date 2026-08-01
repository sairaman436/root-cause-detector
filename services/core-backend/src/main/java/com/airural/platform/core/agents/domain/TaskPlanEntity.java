/*
 * Purpose: Persists a planner-generated task plan.
 * Why it exists: Agent execution must be explainable, inspectable, and recoverable.
 * Architecture fit: Planning record created before routed agent tasks execute.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for task plans. */
@Entity
@Table(name = "task_plans", schema = "agents")
public class TaskPlanEntity {
    @Id private UUID id;
    private UUID conversationId;
    @Column(nullable = false, columnDefinition = "TEXT") private String objective;
    @Column(nullable = false, columnDefinition = "TEXT") private String planJson;
    @Column(nullable = false, length = 40) private String status;
    private UUID createdBy;
    @Column(nullable = false) private Instant createdAt;

    protected TaskPlanEntity() {}

    public TaskPlanEntity(UUID conversationId, String objective, String planJson, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.objective = objective;
        this.planJson = planJson;
        this.status = "PLANNED";
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String objective() { return objective; }
    public String planJson() { return planJson; }
    public String status() { return status; }
}
