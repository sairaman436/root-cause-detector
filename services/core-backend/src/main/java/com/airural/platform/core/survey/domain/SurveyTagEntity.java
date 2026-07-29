/*
 * Purpose: Persists normalized survey tags.
 * Why it exists: Tags are required for survey search, cataloging, and operational grouping.
 * Architecture fit: Search support entity for the survey aggregate.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey tags. */
@Entity
@Table(name = "survey_tags", schema = "survey")
public class SurveyTagEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private Instant createdAt;

    protected SurveyTagEntity() {
    }

    public SurveyTagEntity(SurveyEntity survey, String name) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public String name() { return name; }
}
