/*
 * Purpose: Persists a completed survey submission.
 * Why it exists: The core application flow requires durable field responses linked to a governed survey definition.
 * Architecture fit: Survey bounded-context aggregate for response capture, separate from analytics and AI processing.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

/** JPA entity for survey submissions. */
@Entity
@Table(name = "survey_submissions", schema = "survey")
public class SurveySubmissionEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private UUID submittedByUserId;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(nullable = false)
    private Instant submittedAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveySubmissionAnswerEntity> answers = new ArrayList<>();

    protected SurveySubmissionEntity() {}

    public SurveySubmissionEntity(SurveyEntity survey, UUID submittedByUserId) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.organizationId = survey.organizationId();
        this.submittedByUserId = submittedByUserId;
        this.status = "SUBMITTED";
        this.submittedAt = Instant.now();
        this.updatedAt = this.submittedAt;
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public UUID organizationId() { return organizationId; }
    public UUID submittedByUserId() { return submittedByUserId; }
    public String status() { return status; }
    public Instant submittedAt() { return submittedAt; }
    public List<SurveySubmissionAnswerEntity> answers() { return List.copyOf(answers); }

    /** Adds an answer to this submission. */
    public void addAnswer(SurveyQuestionEntity question, String answerValue) {
        answers.add(new SurveySubmissionAnswerEntity(this, question, answerValue));
        this.updatedAt = Instant.now();
    }
}
