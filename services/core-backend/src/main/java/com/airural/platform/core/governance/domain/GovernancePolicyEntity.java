/*
 * Purpose: Stores configurable AI governance policies.
 * Why it exists: Datasets, models, prompts, agents, inference, and deployments require enforceable lifecycle policies.
 * Architecture fit: Core aggregate for AI-9 policy governance.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Governance policy entity. */
@Entity
@Table(name = "governance_policies", schema = "governance")
public class GovernancePolicyEntity {
    @Id private UUID id;
    private String policyKey;
    private String name;
    private String description;
    private String domain;
    private String severity;
    private String status;
    private String conflictStrategy;
    private UUID ownerId;
    private Instant effectiveFrom;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    protected GovernancePolicyEntity() {}

    /** Creates a governance policy. */
    public GovernancePolicyEntity(UUID id, String policyKey, String name, String description, String domain, String severity, String status, String conflictStrategy, UUID ownerId, Instant effectiveFrom, Instant expiresAt, Instant createdAt, Instant updatedAt) {
        this.id = id; this.policyKey = policyKey; this.name = name; this.description = description; this.domain = domain; this.severity = severity; this.status = status; this.conflictStrategy = conflictStrategy; this.ownerId = ownerId; this.effectiveFrom = effectiveFrom; this.expiresAt = expiresAt; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getPolicyKey() { return policyKey; }
    public String getName() { return name; }
    public String getDomain() { return domain; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
}
