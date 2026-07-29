/*
 * Purpose: Persists selectable options for choice-style survey questions.
 * Why it exists: Single select, multi select, matrix, and lookup questions require governed option catalogs.
 * Architecture fit: Child entity of survey questions.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for question options. */
@Entity
@Table(name = "question_options", schema = "survey")
public class QuestionOptionEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private SurveyQuestionEntity question;

    @Column(name = "option_value", nullable = false, length = 220)
    private String value;

    @Column(nullable = false, length = 220)
    private String label;

    @Column(nullable = false)
    private Integer orderIndex;

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

    protected QuestionOptionEntity() {
    }

    public QuestionOptionEntity(SurveyQuestionEntity question, String value, String label, Integer orderIndex, String metadataJson) {
        this.id = UUID.randomUUID();
        this.question = question;
        this.value = value;
        this.label = label;
        this.orderIndex = orderIndex;
        this.metadataJson = metadataJson;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID questionId() { return question.id(); }
    public String value() { return value; }
    public String label() { return label; }
    public Integer orderIndex() { return orderIndex; }
    public String metadataJson() { return metadataJson; }
}
