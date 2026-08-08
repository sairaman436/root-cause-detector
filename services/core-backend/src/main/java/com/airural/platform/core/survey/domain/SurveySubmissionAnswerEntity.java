/*
 * Purpose: Persists a single answer within a survey submission.
 * Why it exists: Survey responses must retain question linkage and submitted values for retrieval and reporting.
 * Architecture fit: Child entity of survey submissions inside the survey bounded context.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey submission answers. */
@Entity
@Table(name = "survey_submission_answers", schema = "survey")
public class SurveySubmissionAnswerEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private SurveySubmissionEntity submission;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private SurveyQuestionEntity question;

    @Column(nullable = false, length = 120)
    private String questionCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerValue;

    @Column(nullable = false)
    private Instant createdAt;

    protected SurveySubmissionAnswerEntity() {}

    public SurveySubmissionAnswerEntity(SurveySubmissionEntity submission, SurveyQuestionEntity question, String answerValue) {
        this.id = UUID.randomUUID();
        this.submission = submission;
        this.question = question;
        this.questionCode = question.code();
        this.answerValue = answerValue;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID questionId() { return question.id(); }
    public String questionCode() { return questionCode; }
    public String answerValue() { return answerValue; }
    public Instant createdAt() { return createdAt; }
}
