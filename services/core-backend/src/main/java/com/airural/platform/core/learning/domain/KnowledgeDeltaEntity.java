/*
 * Purpose: Stores detected knowledge evolution events.
 * Why it exists: New schemes, policy changes, eligibility updates, agricultural practices, health guidelines, and research updates must trigger refresh and re-index workflows.
 * Architecture fit: Knowledge evolution record for future retrieval and dataset refresh.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Knowledge delta entity. */
@Entity
@Table(name = "knowledge_deltas", schema = "learning")
public class KnowledgeDeltaEntity {
    @Id private UUID id;
    private UUID learningRecordId;
    private String deltaType;
    private String sourceReference;
    @Column(columnDefinition = "TEXT") private String deltaSummary;
    private String refreshJobStatus;
    private String reindexRequestStatus;
    private Instant detectedAt;

    protected KnowledgeDeltaEntity() {}

    /** Creates a knowledge delta. */
    public KnowledgeDeltaEntity(UUID id, UUID learningRecordId, String deltaType, String sourceReference, String deltaSummary, String refreshJobStatus, String reindexRequestStatus, Instant detectedAt) {
        this.id = id; this.learningRecordId = learningRecordId; this.deltaType = deltaType; this.sourceReference = sourceReference; this.deltaSummary = deltaSummary; this.refreshJobStatus = refreshJobStatus; this.reindexRequestStatus = reindexRequestStatus; this.detectedAt = detectedAt;
    }
}
