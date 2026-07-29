/*
 * Purpose: Persists normalized analytical records derived from platform events.
 * Why it exists: Event ingestion must prepare records for future warehouse, feature store, AI training, and reporting pipelines.
 * Architecture fit: Data-pipeline projection owned by the eventing module without implementing analytics products.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for analytical event records. */
@Entity
@Table(name = "analytics_event_records", schema = "eventing")
public class AnalyticsEventRecordEntity {
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

    @Column(nullable = false)
    private Integer schemaVersion;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Instant ingestedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    protected AnalyticsEventRecordEntity() {
    }

    public AnalyticsEventRecordEntity(UUID eventId, String eventType, String topic, String aggregateType, UUID aggregateId, UUID organizationId, UUID actorUserId, Integer schemaVersion, Instant occurredAt, String payloadJson) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.eventType = eventType;
        this.topic = topic;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.schemaVersion = schemaVersion;
        this.occurredAt = occurredAt;
        this.ingestedAt = Instant.now();
        this.payloadJson = payloadJson;
    }

    public UUID id() { return id; }
    public UUID eventId() { return eventId; }
    public String eventType() { return eventType; }
}
