/*
 * Purpose: Persists editable descriptive metadata for an evidence asset.
 * Why it exists: Binary identity fields are immutable, while titles, descriptions, and custom metadata evolve over time.
 * Architecture fit: One-to-one metadata component inside the Evidence aggregate.
 */
package com.airural.platform.core.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for evidence metadata. */
@Entity
@Table(name = "evidence_metadata", schema = "evidence")
public class EvidenceMetadataEntity {
    @Id
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id")
    private EvidenceEntity evidence;

    @Column(length = 220)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String customMetadataJson;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected EvidenceMetadataEntity() {
    }

    public EvidenceMetadataEntity(EvidenceEntity evidence, String title, String description, String customMetadataJson) {
        this.id = UUID.randomUUID();
        this.evidence = evidence;
        this.title = title;
        this.description = description;
        this.customMetadataJson = customMetadataJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public String customMetadataJson() { return customMetadataJson; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    /** Updates editable metadata fields. */
    public void update(String title, String description, String customMetadataJson) {
        this.title = title;
        this.description = description;
        this.customMetadataJson = customMetadataJson;
        this.updatedAt = Instant.now();
    }
}
