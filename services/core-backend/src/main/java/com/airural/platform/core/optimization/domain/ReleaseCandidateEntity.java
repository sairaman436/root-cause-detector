/*
 * Purpose: Stores final release candidate decisions for optimized artifacts and packages.
 * Why it exists: No AI-6 artifact may be released unless optimization, performance, deployment, security, and release reviews pass.
 * Architecture fit: Release governance entity for promotion decisions without performing deployment.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release candidate entity. */
@Entity
@Table(name = "release_candidates", schema = "optimization")
public class ReleaseCandidateEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private String candidateVersion;
    private String status;
    private String promotedBy;
    @Column(columnDefinition = "TEXT")
    private String reviewJson;
    @Column(columnDefinition = "TEXT")
    private String releaseNotes;
    private Instant createdAt;
    private Instant promotedAt;

    protected ReleaseCandidateEntity() {}

    /** Creates a release candidate. */
    public ReleaseCandidateEntity(UUID id, UUID optimizationRunId, String candidateVersion, String status, String promotedBy, String reviewJson, String releaseNotes, Instant createdAt, Instant promotedAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.candidateVersion = candidateVersion; this.status = status; this.promotedBy = promotedBy; this.reviewJson = reviewJson; this.releaseNotes = releaseNotes; this.createdAt = createdAt; this.promotedAt = promotedAt;
    }
}
