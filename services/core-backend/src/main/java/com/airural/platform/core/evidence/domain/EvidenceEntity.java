/*
 * Purpose: Persists governed evidence asset metadata and lifecycle state.
 * Why it exists: Uploaded files need durable references, checksums, ownership, soft-delete state, and search metadata.
 * Architecture fit: Aggregate root for the Evidence and Asset Management module.
 */
package com.airural.platform.core.evidence.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

/** JPA entity for evidence assets. */
@Entity
@Table(name = "evidence", schema = "evidence")
public class EvidenceEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID organizationId;

    private UUID surveyId;
    private UUID questionId;

    @Column(nullable = false)
    private UUID uploadedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EvidenceType evidenceType;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 255)
    private String storedFileName;

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

    @Column(nullable = false)
    private Integer currentVersion;

    private Instant deletedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    @OneToOne(mappedBy = "evidence", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EvidenceMetadataEntity metadata;

    @OneToMany(mappedBy = "evidence", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EvidenceTagEntity> tags = new LinkedHashSet<>();

    protected EvidenceEntity() {
    }

    public EvidenceEntity(
            UUID organizationId,
            UUID surveyId,
            UUID questionId,
            UUID uploadedByUserId,
            EvidenceType evidenceType,
            String originalFileName,
            String storedFileName,
            String mimeType,
            Long sizeBytes,
            String sha256Checksum,
            StorageProvider storageProvider,
            String storageKey,
            String title,
            String description,
            String customMetadataJson,
            Set<String> tagNames) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.surveyId = surveyId;
        this.questionId = questionId;
        this.uploadedByUserId = uploadedByUserId;
        this.evidenceType = evidenceType;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.sha256Checksum = sha256Checksum;
        this.storageProvider = storageProvider;
        this.storageKey = storageKey;
        this.currentVersion = 1;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
        this.metadata = new EvidenceMetadataEntity(this, title, description, customMetadataJson);
        replaceTags(tagNames);
    }

    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public UUID surveyId() { return surveyId; }
    public UUID questionId() { return questionId; }
    public UUID uploadedByUserId() { return uploadedByUserId; }
    public EvidenceType evidenceType() { return evidenceType; }
    public String originalFileName() { return originalFileName; }
    public String storedFileName() { return storedFileName; }
    public String mimeType() { return mimeType; }
    public Long sizeBytes() { return sizeBytes; }
    public String sha256Checksum() { return sha256Checksum; }
    public StorageProvider storageProvider() { return storageProvider; }
    public String storageKey() { return storageKey; }
    public Integer currentVersion() { return currentVersion; }
    public Instant deletedAt() { return deletedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean isActive() { return isActive; }
    public EvidenceMetadataEntity metadata() { return metadata; }
    public Set<EvidenceTagEntity> tags() { return Set.copyOf(tags); }

    /** Updates editable evidence metadata and creates a logical version. */
    public void updateMetadata(String title, String description, String customMetadataJson, Set<String> tagNames) {
        this.metadata.update(title, description, customMetadataJson);
        replaceTags(tagNames);
        this.currentVersion += 1;
        this.updatedAt = Instant.now();
    }

    /** Soft-deletes evidence metadata while retaining the binary for governed retention. */
    public void softDelete() {
        this.isActive = false;
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    /** Restores a previously soft-deleted evidence record. */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
        this.updatedAt = Instant.now();
    }

    private void replaceTags(Set<String> tagNames) {
        Set<String> normalized = tagNames == null ? Set.of() : tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        tags.removeIf(existing -> !normalized.contains(existing.name()));
        Set<String> existingNames = tags.stream()
                .map(EvidenceTagEntity::name)
                .collect(java.util.stream.Collectors.toSet());
        normalized.stream()
                .filter(tag -> !existingNames.contains(tag))
                .forEach(tag -> tags.add(new EvidenceTagEntity(this, tag)));
    }
}
