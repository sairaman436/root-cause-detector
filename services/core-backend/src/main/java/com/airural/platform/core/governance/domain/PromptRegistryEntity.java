/*
 * Purpose: Stores governed prompt versions and ownership metadata.
 * Why it exists: Prompt changes affect AI behavior and must be versioned, risk-classified, approved, and auditable.
 * Architecture fit: Prompt governance registry for AI-9.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Prompt registry entity. */
@Entity
@Table(name = "prompt_registry", schema = "governance")
public class PromptRegistryEntity {
    @Id private UUID id;
    private String promptKey;
    private String version;
    private String ownerRole;
    private String riskClassification;
    private String status;
    private String contentHash;
    private String rollbackVersion;
    private Instant createdAt;
    private Instant updatedAt;

    protected PromptRegistryEntity() {}

    /** Creates a prompt registry record. */
    public PromptRegistryEntity(UUID id, String promptKey, String version, String ownerRole, String riskClassification, String status, String contentHash, String rollbackVersion, Instant createdAt, Instant updatedAt) {
        this.id = id; this.promptKey = promptKey; this.version = version; this.ownerRole = ownerRole; this.riskClassification = riskClassification; this.status = status; this.contentHash = contentHash; this.rollbackVersion = rollbackVersion; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getPromptKey() { return promptKey; }
    public String getVersion() { return version; }
    public String getStatus() { return status; }
}
