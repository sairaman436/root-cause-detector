/*
 * Purpose: Stores compatibility validation for artifacts against runtimes and hardware.
 * Why it exists: AI-6 cannot release artifacts unless runtime and hardware validation passes.
 * Architecture fit: Validation evidence entity for optimization artifacts.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Compatibility report entity. */
@Entity
@Table(name = "compatibility_reports", schema = "optimization")
public class CompatibilityReportEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private UUID artifactId;
    private UUID hardwareProfileId;
    private String runtimeTarget;
    private String compatibilityStatus;
    @Column(columnDefinition = "TEXT")
    private String compatibilityMatrixJson;
    @Column(columnDefinition = "TEXT")
    private String failureDetailsJson;
    private Instant createdAt;

    protected CompatibilityReportEntity() {}

    /** Creates a compatibility report. */
    public CompatibilityReportEntity(UUID id, UUID optimizationRunId, UUID artifactId, UUID hardwareProfileId, String runtimeTarget, String compatibilityStatus, String compatibilityMatrixJson, String failureDetailsJson, Instant createdAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.artifactId = artifactId; this.hardwareProfileId = hardwareProfileId; this.runtimeTarget = runtimeTarget; this.compatibilityStatus = compatibilityStatus; this.compatibilityMatrixJson = compatibilityMatrixJson; this.failureDetailsJson = failureDetailsJson; this.createdAt = createdAt;
    }
}
