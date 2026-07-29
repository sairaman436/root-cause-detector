/*
 * Purpose: Persists immutable survey version snapshots.
 * Why it exists: Survey definitions must retain historical versions for auditability and reproducibility.
 * Architecture fit: Versioning record for the survey aggregate.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey versions. */
@Entity
@Table(name = "survey_versions", schema = "survey")
public class SurveyVersionEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyEntity survey;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String snapshotJson;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean isActive;

    protected SurveyVersionEntity() {
    }

    public SurveyVersionEntity(SurveyEntity survey, Integer versionNumber, String name, String description, String snapshotJson, UUID createdByUserId) {
        this.id = UUID.randomUUID();
        this.survey = survey;
        this.versionNumber = versionNumber;
        this.name = name;
        this.description = description;
        this.snapshotJson = snapshotJson;
        this.createdByUserId = createdByUserId;
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    public UUID id() { return id; }
    public UUID surveyId() { return survey.id(); }
    public Integer versionNumber() { return versionNumber; }
    public String name() { return name; }
    public String description() { return description; }
    public String snapshotJson() { return snapshotJson; }
    public UUID createdByUserId() { return createdByUserId; }
    public Instant createdAt() { return createdAt; }
}
