/*
 * Purpose: Stores trusted knowledge source registry entries.
 * Why it exists: Crawlers and acquisition jobs must know source ownership, trust tier, schedules, and lifecycle.
 * Architecture fit: Operational registry entity for AI-2 knowledge acquisition.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Trusted knowledge source registry entity. */
@Entity
@Table(name = "knowledge_sources", schema = "knowledge")
public class KnowledgeSourceEntity {
    @Id private UUID id;
    private String sourceKey;
    private String name;
    private String sourceType;
    private String baseUrl;
    private String trustTier;
    private String status;
    private String ownerTeam;
    private String scheduleCron;
    private Instant createdAt;
    private Instant updatedAt;

    protected KnowledgeSourceEntity() {}

    /** Creates a trusted knowledge source. */
    public KnowledgeSourceEntity(UUID id, String sourceKey, String name, String sourceType, String baseUrl, String trustTier, String status, String ownerTeam, String scheduleCron, Instant createdAt, Instant updatedAt) {
        this.id = id; this.sourceKey = sourceKey; this.name = name; this.sourceType = sourceType; this.baseUrl = baseUrl; this.trustTier = trustTier; this.status = status; this.ownerTeam = ownerTeam; this.scheduleCron = scheduleCron; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getSourceKey() { return sourceKey; }
    public String getName() { return name; }
    public String getSourceType() { return sourceType; }
    public String getBaseUrl() { return baseUrl; }
    public String getTrustTier() { return trustTier; }
    public String getStatus() { return status; }
    public String getOwnerTeam() { return ownerTeam; }
    public String getScheduleCron() { return scheduleCron; }
}
