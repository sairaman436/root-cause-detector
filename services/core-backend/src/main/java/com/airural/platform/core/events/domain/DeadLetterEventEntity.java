/*
 * Purpose: Persists events that exceeded retry limits or failed consumer processing.
 * Why it exists: Operators need durable dead-letter visibility and replay support.
 * Architecture fit: Eventing module recovery aggregate.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for dead-letter events. */
@Entity
@Table(name = "dead_letter_events", schema = "eventing")
public class DeadLetterEventEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(nullable = false, length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Instant failedAt;

    private Instant replayedAt;

    protected DeadLetterEventEntity() {
    }

    public DeadLetterEventEntity(OutboxEventEntity outbox, String errorMessage) {
        this.id = UUID.randomUUID();
        this.eventId = outbox.id();
        this.eventType = outbox.eventType();
        this.topic = outbox.topic();
        this.aggregateType = outbox.aggregateType();
        this.aggregateId = outbox.aggregateId();
        this.payloadJson = outbox.payloadJson();
        this.errorMessage = errorMessage == null ? "Unknown event failure" : errorMessage.substring(0, Math.min(errorMessage.length(), 1000));
        this.attempts = outbox.attempts();
        this.failedAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID eventId() { return eventId; }
    public String eventType() { return eventType; }
    public String topic() { return topic; }
    public String aggregateType() { return aggregateType; }
    public UUID aggregateId() { return aggregateId; }
    public String payloadJson() { return payloadJson; }
    public String errorMessage() { return errorMessage; }
    public Integer attempts() { return attempts; }
    public Instant failedAt() { return failedAt; }
    public Instant replayedAt() { return replayedAt; }

    public void markReplayed() {
        this.replayedAt = Instant.now();
    }
}
