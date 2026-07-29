/*
 * Purpose: Persists security and identity audit events.
 * Why it exists: Authentication and RBAC actions must leave immutable evidence for compliance.
 * Architecture fit: Implements audit logging for the identity platform.
 */
package com.airural.platform.core.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for an audit event. */
@Entity
@Table(name = "audit_events", schema = "audit")
public class AuditEventEntity {
    @Id
    private UUID id;

    private UUID actorUserId;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuditOutcome outcome;

    @Column(length = 80)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditEventEntity() {
    }

    public AuditEventEntity(
            UUID actorUserId, String eventType, AuditOutcome outcome, String ipAddress, String userAgent, String details) {
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.eventType = eventType;
        this.outcome = outcome;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID actorUserId() { return actorUserId; }
    public String eventType() { return eventType; }
    public AuditOutcome outcome() { return outcome; }
    public Instant createdAt() { return createdAt; }
}
