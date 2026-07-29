/*
 * Purpose: Persists immutable evidence version snapshots.
 * Why it exists: Evidence metadata changes need traceable version history for governance and future AI lineage.
 * Architecture fit: Child entity recording evidence binary identity and metadata snapshots.
 */
package com.airural.platform.core.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for evidence version history. */
@Entity
@Table(name = "evidence_versions", schema = "evidence")
public class EvidenceVersionEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id")
    private EvidenceEntity evidence;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 180)
    private String mimeType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(name = "sha256_checksum", nullable = false, length = 64)
    private String sha256Checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StorageProvider storageProvider;

    @Column(nullable = false, length = 600)
    private String storageKey;

    @Column(columnDefinition = "TEXT")
    private String metadataSnapshotJson;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean isActive;

    protected EvidenceVersionEntity() {
    }

    public EvidenceVersionEntity(EvidenceEntity evidence, String metadataSnapshotJson, UUID createdByUserId) {
        this.id = UUID.randomUUID();
        this.evidence = evidence;
        this.versionNumber = evidence.currentVersion();
        this.originalFileName = evidence.originalFileName();
        this.mimeType = evidence.mimeType();
        this.sizeBytes = evidence.sizeBytes();
        this.sha256Checksum = evidence.sha256Checksum();
        this.storageProvider = evidence.storageProvider();
        this.storageKey = evidence.storageKey();
        this.metadataSnapshotJson = metadataSnapshotJson;
        this.createdByUserId = createdByUserId;
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID evidenceId() { return evidence.id(); }
    public Integer versionNumber() { return versionNumber; }
    public String originalFileName() { return originalFileName; }
    public String mimeType() { return mimeType; }
    public Long sizeBytes() { return sizeBytes; }
    public String sha256Checksum() { return sha256Checksum; }
    public StorageProvider storageProvider() { return storageProvider; }
    public String storageKey() { return storageKey; }
    public String metadataSnapshotJson() { return metadataSnapshotJson; }
    public UUID createdByUserId() { return createdByUserId; }
    public Instant createdAt() { return createdAt; }
}
