/*
 * Purpose: Persists human feedback, overrides, accepted decisions, and rejected decisions.
 * Why it exists: Decision memory and learning datasets need durable human review outcomes.
 * Architecture fit: Feedback record for decision governance.
 */
package com.airural.platform.core.decision.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for decision feedback. */
@Entity
@Table(name = "decision_feedback", schema = "decision")
public class DecisionFeedbackEntity {
    @Id private UUID id;
    @Column(nullable = false) private UUID decisionId;
    private UUID userId;
    @Column(nullable = false, length = 40) private String decisionOutcome;
    @Column(length = 1000) private String feedbackText;
    @Column(nullable = false, columnDefinition = "TEXT") private String overrideJson;
    @Column(nullable = false) private Instant createdAt;

    protected DecisionFeedbackEntity() {}

    public DecisionFeedbackEntity(UUID decisionId, UUID userId, String decisionOutcome, String feedbackText, String overrideJson) {
        this.id = UUID.randomUUID();
        this.decisionId = decisionId;
        this.userId = userId;
        this.decisionOutcome = decisionOutcome;
        this.feedbackText = feedbackText;
        this.overrideJson = overrideJson;
        this.createdAt = Instant.now();
    }
}
