/*
 * Purpose: Tracks enterprise AI risks across datasets, models, prompts, knowledge, security, operations, compliance, and bias.
 * Why it exists: Governance boards need active risk ownership, residual risk, and mitigation visibility before approvals.
 * Architecture fit: Risk management aggregate for AI-9.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Risk register entity. */
@Entity
@Table(name = "risk_register", schema = "governance")
public class RiskRegisterEntity {
    @Id private UUID id;
    private String riskKey;
    private String riskType;
    private String title;
    private String description;
    private String likelihood;
    private String impact;
    private String severity;
    private String status;
    private String mitigationPlan;
    private String ownerRole;
    private Instant dueAt;
    private Instant createdAt;
    private Instant updatedAt;

    protected RiskRegisterEntity() {}

    /** Creates a risk register entry. */
    public RiskRegisterEntity(UUID id, String riskKey, String riskType, String title, String description, String likelihood, String impact, String severity, String status, String mitigationPlan, String ownerRole, Instant dueAt, Instant createdAt, Instant updatedAt) {
        this.id = id; this.riskKey = riskKey; this.riskType = riskType; this.title = title; this.description = description; this.likelihood = likelihood; this.impact = impact; this.severity = severity; this.status = status; this.mitigationPlan = mitigationPlan; this.ownerRole = ownerRole; this.dueAt = dueAt; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getRiskKey() { return riskKey; }
    public String getRiskType() { return riskType; }
    public String getTitle() { return title; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
}
