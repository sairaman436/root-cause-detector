/*
 * Purpose: Persists retry attempt records for outbox publication.
 * Why it exists: Retry attempts must be observable and auditable for production event operations.
 * Architecture fit: Eventing module retry read model.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for event retry attempts. */
@Entity
@Table(name = "event_retry", schema = "eventing")
public class EventRetryEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant scheduledAt;

    private Instant completedAt;

    protected EventRetryEntity() {
    }

    public EventRetryEntity(UUID eventId, Integer attemptNumber, String status, String errorMessage, Instant scheduledAt, Instant completedAt) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.errorMessage = errorMessage == null ? null : errorMessage.substring(0, Math.min(errorMessage.length(), 1000));
        this.scheduledAt = scheduledAt;
        this.completedAt = completedAt;
    }
}
