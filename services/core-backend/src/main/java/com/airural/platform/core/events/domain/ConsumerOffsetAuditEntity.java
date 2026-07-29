/*
 * Purpose: Persists consumer offset and lag snapshots.
 * Why it exists: Operations teams need durable lag evidence for event backbone monitoring.
 * Architecture fit: Event observability store for Kafka consumer groups.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for consumer offset audit records. */
@Entity
@Table(name = "event_consumer_offset", schema = "eventing")
public class ConsumerOffsetAuditEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 180)
    private String consumerGroup;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(nullable = false)
    private Integer partitionId;

    @Column(nullable = false)
    private Long offsetValue;

    private Long lag;

    @Column(nullable = false)
    private Instant recordedAt;

    protected ConsumerOffsetAuditEntity() {
    }

    public ConsumerOffsetAuditEntity(String consumerGroup, String topic, Integer partitionId, Long offsetValue, Long lag) {
        this.id = UUID.randomUUID();
        this.consumerGroup = consumerGroup;
        this.topic = topic;
        this.partitionId = partitionId;
        this.offsetValue = offsetValue;
        this.lag = lag;
        this.recordedAt = Instant.now();
    }
}
