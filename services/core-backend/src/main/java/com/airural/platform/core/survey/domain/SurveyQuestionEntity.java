/*
 * Purpose: Persists dynamic survey questions.
 * Why it exists: The platform needs extensible question definitions, ordering, nesting, defaults, and conditional logic.
 * Architecture fit: Domain entity for the dynamic questionnaire engine.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey questions. */
@Entity
@Table(name = "survey_questions", schema = "survey")
public class SurveyQuestionEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private SurveySectionEntity section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_question_id")
    private SurveyQuestionEntity parentQuestion;

    @Column(nullable = false, length = 120)
    private String code;

    @Column(nullable = false, length = 500)
    private String prompt;

    @Column(length = 1000)
    private String helpText;

    @Column(nullable = false, length = 80)
    private String questionType;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean isRequired;

    @Column(columnDefinition = "TEXT")
    private String defaultValue;

    @Column(columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(columnDefinition = "TEXT")
    private String calculationExpression;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected SurveyQuestionEntity() {
    }

    public SurveyQuestionEntity(
            SurveyEntity survey,
            SurveySectionEntity section,
            SurveyQuestionEntity parentQuestion,
            String code,
            String prompt,
            String helpText,
            String questionType,
            Integer orderIndex,
            boolean required,
            String defaultValue,
            String conditionExpression,
            String calculationExpression,
            String metadataJson) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.section = section;
        this.parentQuestion = parentQuestion;
        this.code = code;
        this.prompt = prompt;
        this.helpText = helpText;
        this.questionType = questionType;
        this.orderIndex = orderIndex;
        this.isRequired = required;
        this.defaultValue = defaultValue;
        this.conditionExpression = conditionExpression;
        this.calculationExpression = calculationExpression;
        this.metadataJson = metadataJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public UUID sectionId() { return section.id(); }
    public UUID parentQuestionId() { return parentQuestion == null ? null : parentQuestion.id(); }
    public String code() { return code; }
    public String prompt() { return prompt; }
    public String helpText() { return helpText; }
    public String questionType() { return questionType; }
    public Integer orderIndex() { return orderIndex; }
    public boolean required() { return isRequired; }
    public String defaultValue() { return defaultValue; }
    public String conditionExpression() { return conditionExpression; }
    public String calculationExpression() { return calculationExpression; }
    public String metadataJson() { return metadataJson; }
}
