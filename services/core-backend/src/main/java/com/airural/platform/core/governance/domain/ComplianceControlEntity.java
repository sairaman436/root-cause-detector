/*
 * Purpose: Maps platform governance controls to external compliance frameworks.
 * Why it exists: ISO, NIST, OWASP, GDPR-ready, and public-sector audits need traceable control evidence.
 * Architecture fit: Compliance matrix storage for AI-9 governance reporting.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Compliance control mapping entity. */
@Entity
@Table(name = "compliance_controls", schema = "governance")
public class ComplianceControlEntity {
    @Id private UUID id;
    private String framework;
    private String controlCode;
    private String title;
    private String description;
    private String implementationStatus;
    private String evidenceRef;
    private String ownerRole;
    private Instant assessedAt;

    protected ComplianceControlEntity() {}

    /** Creates a compliance control mapping. */
    public ComplianceControlEntity(UUID id, String framework, String controlCode, String title, String description, String implementationStatus, String evidenceRef, String ownerRole, Instant assessedAt) {
        this.id = id; this.framework = framework; this.controlCode = controlCode; this.title = title; this.description = description; this.implementationStatus = implementationStatus; this.evidenceRef = evidenceRef; this.ownerRole = ownerRole; this.assessedAt = assessedAt;
    }

    public UUID getId() { return id; }
    public String getFramework() { return framework; }
    public String getControlCode() { return controlCode; }
    public String getTitle() { return title; }
    public String getImplementationStatus() { return implementationStatus; }
}
