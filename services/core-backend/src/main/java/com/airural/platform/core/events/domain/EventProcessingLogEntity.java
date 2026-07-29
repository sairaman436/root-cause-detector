/*
 * Purpose: Persists consumer processing outcomes.
 * Why it exists: Event-driven workflows need traceable processing, latency, and failure records per consumer.
 * Architecture fit: Observability and recovery store for Kafka consumers.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for event processing logs. */
@Entity
@Table(name = "event_processing_log", schema = "eventing")
public class EventProcessingLogEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false, length = 120)
    private String consumerName;

    @Column(nullable = false, length = 180)
    private String topic;

    private Integer partitionId;
    private Long offsetValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EventProcessingStatus status;

    @Column(nullable = false)
    private Integer attempts;

    private Long latencyMs;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant processedAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected EventProcessingLogEntity() {
    }

    public EventProcessingLogEntity(UUID eventId, String consumerName, String topic, Integer partitionId, Long offsetValue, EventProcessingStatus status, int attempts, Long latencyMs, String errorMessage) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.consumerName = consumerName;
        this.topic = topic;
        this.partitionId = partitionId;
        this.offsetValue = offsetValue;
        this.status = status;
        this.attempts = attempts;
        this.latencyMs = latencyMs;
        this.errorMessage = errorMessage == null ? null : errorMessage.substring(0, Math.min(errorMessage.length(), 1000));
        this.processedAt = Instant.now();
        this.createdAt = this.processedAt;
    }

    public UUID id() { return id; }
    public UUID eventId() { return eventId; }
    public String consumerName() { return consumerName; }
    public EventProcessingStatus status() { return status; }
}
