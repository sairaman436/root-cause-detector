/*
 * Purpose: Persists immutable event log records after events are created or replayed.
 * Why it exists: Operators need queryable event history independent of Kafka retention.
 * Architecture fit: Eventing module read model for event APIs, replay, and audit visibility.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for event log records. */
@Entity
@Table(name = "event_log", schema = "eventing")
public class EventLogEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    private UUID organizationId;
    private UUID actorUserId;

    @Column(nullable = false, length = 120)
    private String producerService;

    @Column(nullable = false, length = 120)
    private String correlationId;

    @Column(length = 120)
    private String traceId;

    @Column(nullable = false)
    private Integer schemaVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Instant loggedAt;

    protected EventLogEntity() {
    }

    public EventLogEntity(OutboxEventEntity outbox) {
        this.id = UUID.randomUUID();
        this.eventId = outbox.id();
        this.eventType = outbox.eventType();
        this.topic = outbox.topic();
        this.aggregateType = outbox.aggregateType();
        this.aggregateId = outbox.aggregateId();
        this.organizationId = outbox.organizationId();
        this.actorUserId = outbox.actorUserId();
        this.producerService = outbox.producerService();
        this.correlationId = outbox.correlationId();
        this.traceId = outbox.traceId();
        this.schemaVersion = outbox.schemaVersion();
        this.payloadJson = outbox.payloadJson();
        this.metadataJson = outbox.metadataJson();
        this.occurredAt = outbox.occurredAt();
        this.loggedAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID eventId() { return eventId; }
    public String eventType() { return eventType; }
    public String topic() { return topic; }
    public String aggregateType() { return aggregateType; }
    public UUID aggregateId() { return aggregateId; }
    public UUID organizationId() { return organizationId; }
    public UUID actorUserId() { return actorUserId; }
    public String producerService() { return producerService; }
    public String correlationId() { return correlationId; }
    public String traceId() { return traceId; }
    public Integer schemaVersion() { return schemaVersion; }
    public String payloadJson() { return payloadJson; }
    public String metadataJson() { return metadataJson; }
    public Instant occurredAt() { return occurredAt; }
    public Instant loggedAt() { return loggedAt; }
}
