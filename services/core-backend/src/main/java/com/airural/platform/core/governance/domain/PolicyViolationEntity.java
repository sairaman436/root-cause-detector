/*
 * Purpose: Records policy violations detected across governed AI workflows.
 * Why it exists: Governance observability needs violation counts, severity, remediation status, and artifact traceability.
 * Architecture fit: AI-9 policy enforcement and monitoring record.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Policy violation entity. */
@Entity
@Table(name = "policy_violations", schema = "governance")
public class PolicyViolationEntity {
    @Id private UUID id;
    private UUID policyId;
    private String artifactType;
    private String artifactRef;
    private String violationType;
    private String severity;
    private String status;
    private String remediation;
    private Instant detectedAt;

    protected PolicyViolationEntity() {}

    /** Creates a policy violation. */
    public PolicyViolationEntity(UUID id, UUID policyId, String artifactType, String artifactRef, String violationType, String severity, String status, String remediation, Instant detectedAt) {
        this.id = id; this.policyId = policyId; this.artifactType = artifactType; this.artifactRef = artifactRef; this.violationType = violationType; this.severity = severity; this.status = status; this.remediation = remediation; this.detectedAt = detectedAt;
    }

    public UUID getId() { return id; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
}
