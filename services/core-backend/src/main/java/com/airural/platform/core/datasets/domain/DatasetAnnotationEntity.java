/*
 * Purpose: Stores human and expert annotation decisions.
 * Why it exists: AI datasets need review history, conflict resolution, and approval evidence.
 * Architecture fit: Annotation platform entity for AI-1.
 */
package com.airural.platform.core.datasets.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Dataset annotation entity. */
@Entity
@Table(name = "dataset_annotations", schema = "datasets")
public class DatasetAnnotationEntity {
    @Id private UUID id;
    private UUID sampleId;
    private UUID reviewerId;
    private String annotationType;
    private String label;
    @Column(columnDefinition = "TEXT")
    private String notes;
    private String status;
    private Instant createdAt;
    protected DatasetAnnotationEntity() {}
    public DatasetAnnotationEntity(UUID id, UUID sampleId, UUID reviewerId, String annotationType, String label, String notes, String status, Instant createdAt) {
        this.id = id; this.sampleId = sampleId; this.reviewerId = reviewerId; this.annotationType = annotationType; this.label = label; this.notes = notes; this.status = status; this.createdAt = createdAt;
    }
}
