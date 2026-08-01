/*
 * Purpose: Tracks release candidates through regression validation and board certification.
 * Why it exists: Stable and LTS releases must pass certification gates before production release.
 * Architecture fit: AI-10 candidate workflow entity.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release candidate entity. */
@Entity(name = "ModelReleaseCandidateEntity")
@Table(name = "release_candidates", schema = "model_release")
public class ReleaseCandidateEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String candidateTag;
    private String status;
    private String validationStatus;
    private String regressionStatus;
    private Instant createdAt;

    protected ReleaseCandidateEntity() {}

    /** Creates a release candidate. */
    public ReleaseCandidateEntity(UUID id, UUID releaseVersionId, String candidateTag, String status, String validationStatus, String regressionStatus, Instant createdAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.candidateTag = candidateTag; this.status = status; this.validationStatus = validationStatus; this.regressionStatus = regressionStatus; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getCandidateTag() { return candidateTag; }
    public String getStatus() { return status; }
    public String getValidationStatus() { return validationStatus; }
}
