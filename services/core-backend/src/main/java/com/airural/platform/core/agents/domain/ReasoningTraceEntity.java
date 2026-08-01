/*
 * Purpose: Persists explainable agent reasoning steps.
 * Why it exists: Human reviewers need traceable, cited reasoning before acting on agent suggestions.
 * Architecture fit: Explainability record for the multi-agent execution engine.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for reasoning traces. */
@Entity
@Table(name = "reasoning_traces", schema = "agents")
public class ReasoningTraceEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID executionId;
    private UUID taskId;
    @Column(nullable = false) private Integer stepNumber;
    @Column(nullable = false, length = 120) private String agentKey;
    @Column(nullable = false, length = 80) private String reasoningType;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Column(nullable = false, columnDefinition = "TEXT") private String citationsJson;
    @Column(nullable = false) private Double confidence;
    @Column(nullable = false) private Instant createdAt;

    protected ReasoningTraceEntity() {}

    public ReasoningTraceEntity(UUID executionId, UUID taskId, Integer stepNumber, String agentKey, String reasoningType, String content, String citationsJson, Double confidence) {
        this.id = UUID.randomUUID();
        this.executionId = executionId;
        this.taskId = taskId;
        this.stepNumber = stepNumber;
        this.agentKey = agentKey;
        this.reasoningType = reasoningType;
        this.content = content;
        this.citationsJson = citationsJson;
        this.confidence = confidence;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public String agentKey() { return agentKey; }
    public String content() { return content; }
    public Double confidence() { return confidence; }
}
