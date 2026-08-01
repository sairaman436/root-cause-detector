/*
 * Purpose: Stores release adoption and operational metrics.
 * Why it exists: Release engineering must track downloads, deployments, failures, rollback rate, compatibility, and adoption.
 * Architecture fit: AI-10 observability record for release lifecycle reporting.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release metrics entity. */
@Entity
@Table(name = "release_metrics", schema = "model_release")
public class ReleaseMetricsEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private Long downloads;
    private Long deployments;
    private Long failures;
    private Double rollbackRate;
    private Double compatibilityScore;
    private Double adoptionScore;
    private Instant capturedAt;

    protected ReleaseMetricsEntity() {}

    /** Creates release metrics. */
    public ReleaseMetricsEntity(UUID id, UUID releaseVersionId, Long downloads, Long deployments, Long failures, Double rollbackRate, Double compatibilityScore, Double adoptionScore, Instant capturedAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.downloads = downloads; this.deployments = deployments; this.failures = failures; this.rollbackRate = rollbackRate; this.compatibilityScore = compatibilityScore; this.adoptionScore = adoptionScore; this.capturedAt = capturedAt;
    }
}
