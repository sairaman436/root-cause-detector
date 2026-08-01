package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Reviewer assigned to dataset annotation and approval work. */
@Entity
@Table(name = "dataset_reviewers", schema = "datasets")
public class DatasetReviewerEntity {
    @Id private UUID id;
    private UUID userId;
    private String reviewerType;
    private String expertise;
    private Integer assignedCount;
    private Integer completedCount;
    private Instant createdAt;
    protected DatasetReviewerEntity() {}
    public DatasetReviewerEntity(UUID id, UUID userId, String reviewerType, String expertise, Integer assignedCount, Integer completedCount, Instant createdAt) {
        this.id = id; this.userId = userId; this.reviewerType = reviewerType; this.expertise = expertise; this.assignedCount = assignedCount; this.completedCount = completedCount; this.createdAt = createdAt;
    }
}
