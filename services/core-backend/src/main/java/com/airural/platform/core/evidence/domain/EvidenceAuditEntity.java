/*
 * Purpose: Persists immutable evidence audit events.
 * Why it exists: Upload, download, update, delete, restore, and signed URL requests must be traceable.
 * Architecture fit: Module-local audit table complementing the global identity audit log.
 */
package com.airural.platform.core.evidence.domain;

import com.airural.platform.core.identity.domain.AuditOutcome;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for evidence audit events. */
@Entity
@Table(name = "evidence_audit", schema = "evidence")
public class EvidenceAuditEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id")
    private EvidenceEntity evidence;

    @Column(nullable = false)
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private EvidenceAuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditOutcome outcome;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private Instant createdAt;

    protected EvidenceAuditEntity() {
    }

    public EvidenceAuditEntity(EvidenceEntity evidence, UUID actorUserId, EvidenceAuditAction action, AuditOutcome outcome, String details) {
        this.id = UUID.randomUUID();
        this.evidence = evidence;
        this.actorUserId = actorUserId;
        this.action = action;
        this.outcome = outcome;
        this.details = details;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID evidenceId() { return evidence.id(); }
    public UUID actorUserId() { return actorUserId; }
    public EvidenceAuditAction action() { return action; }
    public AuditOutcome outcome() { return outcome; }
    public String details() { return details; }
    public Instant createdAt() { return createdAt; }
}
