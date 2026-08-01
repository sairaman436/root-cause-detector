/*
 * Purpose: Stores document fingerprints and duplicate decisions.
 * Why it exists: Continuous acquisition must detect duplicate and unchanged source documents.
 * Architecture fit: Fingerprinting entity for the AI-2 acquisition quality engine.
 */
package com.airural.platform.core.knowledge.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Knowledge document fingerprint entity. */
@Entity
@Table(name = "knowledge_fingerprints", schema = "knowledge")
public class KnowledgeFingerprintEntity {
    @Id private UUID id;
    private UUID sourceId;
    private String fingerprint;
    private String algorithm;
    private Boolean duplicate;
    private Instant firstSeenAt;

    protected KnowledgeFingerprintEntity() {}

    /** Creates a document fingerprint record. */
    public KnowledgeFingerprintEntity(UUID id, UUID sourceId, String fingerprint, String algorithm, Boolean duplicate, Instant firstSeenAt) {
        this.id = id; this.sourceId = sourceId; this.fingerprint = fingerprint; this.algorithm = algorithm; this.duplicate = duplicate; this.firstSeenAt = firstSeenAt;
    }
}
