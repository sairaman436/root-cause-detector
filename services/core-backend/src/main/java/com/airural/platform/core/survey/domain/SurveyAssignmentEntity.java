/*
 * Purpose: Persists survey assignment targets.
 * Why it exists: Survey rollouts must be targeted to organizations, teams, users, and regions.
 * Architecture fit: Assignment entity for governed survey distribution.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey assignments. */
@Entity
@Table(name = "survey_assignments", schema = "survey")
public class SurveyAssignmentEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssignmentType assignmentType;

    @Column(nullable = false, length = 160)
    private String targetId;

    @Column(length = 220)
    private String targetName;

    @Column(nullable = false)
    private UUID assignedByUserId;

    @Column(nullable = false)
    private Instant assignedAt;

    private Instant dueAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected SurveyAssignmentEntity() {
    }

    public SurveyAssignmentEntity(
            SurveyEntity survey,
            AssignmentType assignmentType,
            String targetId,
            String targetName,
            UUID assignedByUserId,
            Instant dueAt) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.assignmentType = assignmentType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.assignedByUserId = assignedByUserId;
        this.assignedAt = Instant.now();
        this.dueAt = dueAt;
        this.createdAt = this.assignedAt;
        this.updatedAt = this.assignedAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public AssignmentType assignmentType() { return assignmentType; }
    public String targetId() { return targetId; }
    public String targetName() { return targetName; }
    public UUID assignedByUserId() { return assignedByUserId; }
    public Instant assignedAt() { return assignedAt; }
    public Instant dueAt() { return dueAt; }
}
