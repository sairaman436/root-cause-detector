/*
 * Purpose: Records release lifecycle history.
 * Why it exists: Promotion, rollback, hotfix, deprecation, and retirement decisions need an immutable operational timeline.
 * Architecture fit: AI-10 release audit and history record.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release history entity. */
@Entity
@Table(name = "release_history", schema = "model_release")
public class ReleaseHistoryEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String rationale;
    private UUID actorId;
    private String eventHash;
    private Instant createdAt;

    protected ReleaseHistoryEntity() {}

    /** Creates a release history event. */
    public ReleaseHistoryEntity(UUID id, UUID releaseVersionId, String eventType, String fromStatus, String toStatus, String rationale, UUID actorId, String eventHash, Instant createdAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.eventType = eventType; this.fromStatus = fromStatus; this.toStatus = toStatus; this.rationale = rationale; this.actorId = actorId; this.eventHash = eventHash; this.createdAt = createdAt;
    }
}
