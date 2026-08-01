/*
 * Purpose: Persists human feedback and approval decisions for agent outputs.
 * Why it exists: Consequential agent recommendations require human review and continuous quality feedback.
 * Architecture fit: Feedback record for agent evaluation and governance.
 */
package com.airural.platform.core.agents.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for agent feedback. */
@Entity
@Table(name = "agent_feedback", schema = "agents")
public class AgentFeedbackEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID executionId;
    private UUID userId;
    @Column(nullable = false) private Integer rating;
    @Column(length = 1000) private String comment;
    @Column(nullable = false, length = 40) private String approvalDecision;
    @Column(nullable = false) private Instant createdAt;

    protected AgentFeedbackEntity() {}

    public AgentFeedbackEntity(UUID executionId, UUID userId, Integer rating, String comment, String approvalDecision) {
        this.id = UUID.randomUUID();
        this.executionId = executionId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.approvalDecision = approvalDecision;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID executionId() { return executionId; }
    public Integer rating() { return rating; }
    public String approvalDecision() { return approvalDecision; }
}
