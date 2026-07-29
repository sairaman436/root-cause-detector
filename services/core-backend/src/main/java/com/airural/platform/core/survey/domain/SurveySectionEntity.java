/*
 * Purpose: Persists ordered sections inside a survey questionnaire.
 * Why it exists: Questionnaire structure requires grouping, nested sections, and conditional display.
 * Architecture fit: Domain entity for the dynamic questionnaire engine.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey sections. */
@Entity
@Table(name = "survey_sections", schema = "survey")
public class SurveySectionEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_section_id")
    private SurveySectionEntity parentSection;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private boolean isRepeatable;

    @Column(columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected SurveySectionEntity() {
    }

    public SurveySectionEntity(
            SurveyEntity survey,
            SurveySectionEntity parentSection,
            String code,
            String title,
            String description,
            Integer orderIndex,
            boolean repeatable,
            String conditionExpression) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.parentSection = parentSection;
        this.code = code;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.isRepeatable = repeatable;
        this.conditionExpression = conditionExpression;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public UUID parentSectionId() { return parentSection == null ? null : parentSection.id(); }
    public String code() { return code; }
    public String title() { return title; }
    public String description() { return description; }
    public Integer orderIndex() { return orderIndex; }
    public boolean repeatable() { return isRepeatable; }
    public String conditionExpression() { return conditionExpression; }
}
