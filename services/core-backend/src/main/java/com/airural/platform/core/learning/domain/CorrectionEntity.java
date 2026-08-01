/*
 * Purpose: Stores expert corrections and improved explanations.
 * Why it exists: Human edited and accepted outputs must remain separate from raw AI output for future training governance.
 * Architecture fit: Quality evidence entity for continuous learning records.
 */
package com.airural.platform.core.learning.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Correction entity. */
@Entity
@Table(name = "corrections", schema = "learning")
public class CorrectionEntity {
    @Id private UUID id;
    private UUID learningRecordId;
    private String correctionType;
    @Column(columnDefinition = "TEXT") private String originalText;
    @Column(columnDefinition = "TEXT") private String correctedText;
    private String correctedBy;
    private Instant createdAt;

    protected CorrectionEntity() {}

    /** Creates a correction. */
    public CorrectionEntity(UUID id, UUID learningRecordId, String correctionType, String originalText, String correctedText, String correctedBy, Instant createdAt) {
        this.id = id; this.learningRecordId = learningRecordId; this.correctionType = correctionType; this.originalText = originalText; this.correctedText = correctedText; this.correctedBy = correctedBy; this.createdAt = createdAt;
    }
}
