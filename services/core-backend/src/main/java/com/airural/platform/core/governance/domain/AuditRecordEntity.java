/*
 * Purpose: Stores immutable governance audit records.
 * Why it exists: Training, evaluation, inference, dataset, prompt, policy, agent, deployment, and rollback actions need tamper-evident trails.
 * Architecture fit: AI-9 immutable audit ledger for governance accountability.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Governance audit record entity. */
@Entity
@Table(name = "audit_records", schema = "governance")
public class AuditRecordEntity {
    @Id private UUID id;
    private String eventType;
    private String artifactType;
    private String artifactRef;
    private UUID actorId;
    private String decision;
    private String evidenceJson;
    private String policyComplianceStatus;
    private String eventHash;
    private String previousHash;
    private Instant createdAt;

    protected AuditRecordEntity() {}

    /** Creates an immutable audit record. */
    public AuditRecordEntity(UUID id, String eventType, String artifactType, String artifactRef, UUID actorId, String decision, String evidenceJson, String policyComplianceStatus, String eventHash, String previousHash, Instant createdAt) {
        this.id = id; this.eventType = eventType; this.artifactType = artifactType; this.artifactRef = artifactRef; this.actorId = actorId; this.decision = decision; this.evidenceJson = evidenceJson; this.policyComplianceStatus = policyComplianceStatus; this.eventHash = eventHash; this.previousHash = previousHash; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getArtifactType() { return artifactType; }
    public String getArtifactRef() { return artifactRef; }
    public String getEventHash() { return eventHash; }
    public Instant getCreatedAt() { return createdAt; }
}
