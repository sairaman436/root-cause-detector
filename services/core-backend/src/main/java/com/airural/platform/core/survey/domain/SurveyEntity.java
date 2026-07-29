/*
 * Purpose: Persists a governed survey definition.
 * Why it exists: Surveys are the core evidence collection instrument for downstream decision intelligence.
 * Architecture fit: Aggregate root for survey management, workflow, versioning, tags, and assignments.
 */
package com.airural.platform.core.survey.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

/** JPA entity for surveys. */
@Entity
@Table(name = "surveys", schema = "survey")
public class SurveyEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private SurveyTemplateEntity template;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SurveyStatus status;

    @Column(nullable = false)
    private Integer currentVersion;

    private UUID clonedFromSurveyId;
    private Instant archivedAt;
    private Instant deletedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Integer version;

    @Column(nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<SurveyStatusHistoryEntity> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SurveyTagEntity> tags = new LinkedHashSet<>();

    protected SurveyEntity() {
    }

    public SurveyEntity(
            SurveyTemplateEntity template,
            UUID organizationId,
            UUID createdByUserId,
            String name,
            String description,
            Set<String> tagNames) {
        this.id = UUID.randomUUID();
        this.template = template;
        this.organizationId = organizationId;
        this.createdByUserId = createdByUserId;
        this.name = name;
        this.description = description;
        this.status = SurveyStatus.DRAFT;
        this.currentVersion = 1;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.isActive = true;
        replaceTags(tagNames);
        addStatusHistory(null, SurveyStatus.DRAFT, createdByUserId, "Survey created");
    }

    public UUID id() { return id; }
    public SurveyTemplateEntity template() { return template; }
    public UUID organizationId() { return organizationId; }
    public UUID createdByUserId() { return createdByUserId; }
    public String name() { return name; }
    public String description() { return description; }
    public SurveyStatus status() { return status; }
    public Integer currentVersion() { return currentVersion; }
    public UUID clonedFromSurveyId() { return clonedFromSurveyId; }
    public Instant archivedAt() { return archivedAt; }
    public Instant deletedAt() { return deletedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public boolean isActive() { return isActive; }
    public Set<SurveyTagEntity> tags() { return Set.copyOf(tags); }

    /** Updates editable survey metadata and creates a new version number. */
    public void update(String name, String description, Set<String> tagNames) {
        ensureEditable();
        this.name = name;
        this.description = description;
        this.currentVersion += 1;
        replaceTags(tagNames);
        this.updatedAt = Instant.now();
    }

    /** Moves the survey through a validated workflow transition. */
    public void transitionTo(SurveyStatus nextStatus, UUID actorUserId, String reason) {
        if (!status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException("Invalid survey status transition from " + status + " to " + nextStatus);
        }
        SurveyStatus previous = this.status;
        this.status = nextStatus;
        if (nextStatus == SurveyStatus.ARCHIVED) {
            this.archivedAt = Instant.now();
        }
        if (nextStatus == SurveyStatus.DELETED) {
            this.deletedAt = Instant.now();
            this.isActive = false;
        }
        this.updatedAt = Instant.now();
        addStatusHistory(previous, nextStatus, actorUserId, reason);
    }

    /** Marks this survey as cloned from another survey. */
    public void markClonedFrom(UUID sourceSurveyId) {
        this.clonedFromSurveyId = sourceSurveyId;
    }

    private void ensureEditable() {
        if (!Set.of(SurveyStatus.DRAFT, SurveyStatus.REVIEW).contains(status)) {
            throw new IllegalStateException("Only draft or review surveys can be edited");
        }
    }

    private void replaceTags(Set<String> tagNames) {
        tags.clear();
        if (tagNames != null) {
            tagNames.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(tag -> !tag.isBlank())
                    .map(String::toLowerCase)
                    .distinct()
                    .forEach(tag -> tags.add(new SurveyTagEntity(this, tag)));
        }
    }

    private void addStatusHistory(SurveyStatus from, SurveyStatus to, UUID actorUserId, String reason) {
        statusHistory.add(new SurveyStatusHistoryEntity(this, from, to, actorUserId, reason));
    }
}
