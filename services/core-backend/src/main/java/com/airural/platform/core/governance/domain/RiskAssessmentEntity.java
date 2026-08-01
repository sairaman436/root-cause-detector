/*
 * Purpose: Records point-in-time risk assessments for governed AI artifacts.
 * Why it exists: Approval decisions require evidence of risk scoring, reviewer identity, and residual risk.
 * Architecture fit: Supports AI-9 risk lifecycle and governance board review.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Risk assessment entity. */
@Entity
@Table(name = "risk_assessments", schema = "governance")
public class RiskAssessmentEntity {
    @Id private UUID id;
    private UUID riskId;
    private String artifactType;
    private String artifactRef;
    private Integer inherentScore;
    private Integer residualScore;
    private String assessmentNotes;
    private UUID assessedBy;
    private Instant assessedAt;

    protected RiskAssessmentEntity() {}

    /** Creates a risk assessment. */
    public RiskAssessmentEntity(UUID id, UUID riskId, String artifactType, String artifactRef, Integer inherentScore, Integer residualScore, String assessmentNotes, UUID assessedBy, Instant assessedAt) {
        this.id = id; this.riskId = riskId; this.artifactType = artifactType; this.artifactRef = artifactRef; this.inherentScore = inherentScore; this.residualScore = residualScore; this.assessmentNotes = assessmentNotes; this.assessedBy = assessedBy; this.assessedAt = assessedAt;
    }

    public UUID getId() { return id; }
    public String getArtifactType() { return artifactType; }
    public Integer getResidualScore() { return residualScore; }
}
