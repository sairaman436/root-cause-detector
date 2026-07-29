/*
 * Purpose: Persists normalized evidence tags.
 * Why it exists: Evidence search and downstream data discovery require governed tag filters.
 * Architecture fit: Child entity owned by the Evidence aggregate.
 */
package com.airural.platform.core.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for evidence tags. */
@Entity
@Table(name = "evidence_tags", schema = "evidence")
public class EvidenceTagEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id")
    private EvidenceEntity evidence;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private Instant createdAt;

    protected EvidenceTagEntity() {
    }

    public EvidenceTagEntity(EvidenceEntity evidence, String name) {
        this.id = UUID.randomUUID();
        this.evidence = evidence;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID evidenceId() { return evidence.id(); }
    public String name() { return name; }
    public Instant createdAt() { return createdAt; }
}
