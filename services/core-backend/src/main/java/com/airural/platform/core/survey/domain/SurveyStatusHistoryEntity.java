/*
 * Purpose: Persists survey workflow transition history.
 * Why it exists: Survey lifecycle governance requires auditable status transitions.
 * Architecture fit: Workflow audit entity for survey management.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey status history. */
@Entity
@Table(name = "survey_status_history", schema = "survey")
public class SurveyStatusHistoryEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private SurveyStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SurveyStatus toStatus;

    @Column(nullable = false)
    private UUID changedByUserId;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private Instant createdAt;

    protected SurveyStatusHistoryEntity() {
    }

    public SurveyStatusHistoryEntity(SurveyEntity survey, SurveyStatus fromStatus, SurveyStatus toStatus, UUID changedByUserId, String reason) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public SurveyStatus fromStatus() { return fromStatus; }
    public SurveyStatus toStatus() { return toStatus; }
    public UUID changedByUserId() { return changedByUserId; }
    public String reason() { return reason; }
    public Instant createdAt() { return createdAt; }
}
