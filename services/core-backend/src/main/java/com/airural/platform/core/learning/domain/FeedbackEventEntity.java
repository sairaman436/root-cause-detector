/*
 * Purpose: Stores human, expert, agent, decision-engine, and knowledge feedback events.
 * Why it exists: Feedback sources must be auditable before becoming learning candidates.
 * Architecture fit: Child evidence entity under a learning record.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Feedback event entity. */
@Entity
@Table(name = "feedback_events", schema = "learning")
public class FeedbackEventEntity {
    @Id private UUID id;
    private UUID learningRecordId;
    private String feedbackSource;
    private String feedbackType;
    @Column(columnDefinition = "TEXT") private String feedbackText;
    private String sentiment;
    private Instant createdAt;

    protected FeedbackEventEntity() {}

    /** Creates a feedback event. */
    public FeedbackEventEntity(UUID id, UUID learningRecordId, String feedbackSource, String feedbackType, String feedbackText, String sentiment, Instant createdAt) {
        this.id = id; this.learningRecordId = learningRecordId; this.feedbackSource = feedbackSource; this.feedbackType = feedbackType; this.feedbackText = feedbackText; this.sentiment = sentiment; this.createdAt = createdAt;
    }
}
