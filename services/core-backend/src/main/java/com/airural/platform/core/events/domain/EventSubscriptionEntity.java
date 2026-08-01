/*
 * Purpose: Persists configured event consumer subscriptions.
 * Why it exists: Operators need visibility into active consumers and future integration monitors across topics.
 * Architecture fit: Eventing module configuration read model.
 */
package com.airural.platform.core.events.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for event subscriptions. */
@Entity
@Table(name = "event_subscription", schema = "eventing")
public class EventSubscriptionEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String consumerName;

    @Column(nullable = false, length = 180)
    private String topic;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected EventSubscriptionEntity() {
    }

    public UUID id() { return id; }
    public String consumerName() { return consumerName; }
    public String topic() { return topic; }
    public String status() { return status; }
    public String description() { return description; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
