/*
 * Purpose: Persists validation rules for survey questions and cross-field checks.
 * Why it exists: Questionnaire quality depends on required, regex, range, date, cross-field, and custom validation.
 * Architecture fit: Domain entity for the validation engine.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for validation rules. */
@Entity
@Table(name = "validation_rules", schema = "survey")
public class ValidationRuleEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private SurveyQuestionEntity question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private ValidationRuleType ruleType;

    @Column(columnDefinition = "TEXT")
    private String expression;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String paramsJson;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected ValidationRuleEntity() {
    }

    public ValidationRuleEntity(
            SurveyEntity survey,
            SurveyQuestionEntity question,
            ValidationRuleType ruleType,
            String expression,
            String message,
            String paramsJson,
            Integer orderIndex) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.question = question;
        this.ruleType = ruleType;
        this.expression = expression;
        this.message = message;
        this.paramsJson = paramsJson;
        this.orderIndex = orderIndex;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public UUID questionId() { return question == null ? null : question.id(); }
    public ValidationRuleType ruleType() { return ruleType; }
    public String expression() { return expression; }
    public String message() { return message; }
    public String paramsJson() { return paramsJson; }
    public Integer orderIndex() { return orderIndex; }
}
