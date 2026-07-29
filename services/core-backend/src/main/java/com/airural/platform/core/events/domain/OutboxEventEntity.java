/*
 * Purpose: Persists events atomically with domain transactions.
 * Why it exists: The transactional outbox pattern prevents lost events when Kafka is temporarily unavailable.
 * Architecture fit: Aggregate root for reliable event publication.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

/** JPA entity for transactional outbox events. */
@Entity
@Table(name = "outbox_events", schema = "eventing")
public class OutboxEventEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false)
    private Integer schemaVersion;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    private UUID organizationId;
    private UUID actorUserId;

    @Column(nullable = false, length = 120)
    private String correlationId;

    @Column(nullable = false, length = 120)
    private String producerService;

    @Column(length = 120)
    private String traceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OutboxStatus status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    @Column(length = 1000)
    private String lastError;

    @Column(nullable = false)
    private Instant occurredAt;

    private Instant publishedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            UUID id,
            String eventType,
            Integer schemaVersion,
            String topic,
            String aggregateType,
            UUID aggregateId,
            UUID organizationId,
            UUID actorUserId,
            String correlationId,
            String producerService,
            String traceId,
            String payloadJson,
            String metadataJson,
            Instant occurredAt) {
        this.id = id;
        this.eventType = eventType;
        this.schemaVersion = schemaVersion;
        this.topic = topic;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.correlationId = correlationId;
        this.producerService = producerService;
        this.traceId = traceId;
        this.payloadJson = payloadJson;
        this.metadataJson = metadataJson;
        this.status = OutboxStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = Instant.now();
        this.occurredAt = occurredAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public String eventType() { return eventType; }
    public Integer schemaVersion() { return schemaVersion; }
    public String topic() { return topic; }
    public String aggregateType() { return aggregateType; }
    public UUID aggregateId() { return aggregateId; }
    public UUID organizationId() { return organizationId; }
    public UUID actorUserId() { return actorUserId; }
    public String correlationId() { return correlationId; }
    public String producerService() { return producerService; }
    public String traceId() { return traceId; }
    public String payloadJson() { return payloadJson; }
    public String metadataJson() { return metadataJson; }
    public OutboxStatus status() { return status; }
    public Integer attempts() { return attempts; }
    public Instant nextAttemptAt() { return nextAttemptAt; }
    public String lastError() { return lastError; }
    public Instant occurredAt() { return occurredAt; }
    public Instant publishedAt() { return publishedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    /** Marks this event as successfully published to Kafka. */
    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.updatedAt = this.publishedAt;
    }

    /** Records a publish failure and schedules retry or dead-letter state. */
    public void markFailure(String error, int maxAttempts) {
        this.attempts += 1;
        this.lastError = error == null ? "Unknown publish error" : error.substring(0, Math.min(error.length(), 1000));
        this.status = attempts >= maxAttempts ? OutboxStatus.DEAD_LETTERED : OutboxStatus.FAILED;
        this.nextAttemptAt = Instant.now().plusSeconds(Math.min(300, (long) Math.pow(2, Math.min(attempts, 8))));
        this.updatedAt = Instant.now();
    }

    /** Returns an event to pending state for the next retry. */
    public void markRetryPending() {
        this.status = OutboxStatus.PENDING;
        this.updatedAt = Instant.now();
    }
}
