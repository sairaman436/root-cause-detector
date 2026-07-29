/*
 * Purpose: Persists reusable survey templates.
 * Why it exists: Survey creation should support governed template library reuse.
 * Architecture fit: Template aggregate root for the survey domain.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** JPA entity for survey templates. */
@Entity
@Table(name = "survey_templates", schema = "survey")
public class SurveyTemplateEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private SurveyTemplateCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SurveyTemplateStatus status;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    protected SurveyTemplateEntity() {
    }

    public SurveyTemplateEntity(
            String name,
            String description,
            SurveyTemplateCategory category,
            SurveyTemplateStatus status,
            String metadataJson,
            UUID createdByUserId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.metadataJson = metadataJson;
        this.createdByUserId = createdByUserId;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public SurveyTemplateCategory category() { return category; }
    public SurveyTemplateStatus status() { return status; }
    public String metadataJson() { return metadataJson; }
    public UUID createdByUserId() { return createdByUserId; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    /** Updates editable metadata for the template. */
    public void update(String name, String description, SurveyTemplateCategory category, SurveyTemplateStatus status, String metadataJson) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.metadataJson = metadataJson;
        this.updatedAt = Instant.now();
    }
}
