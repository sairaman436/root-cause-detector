/*
 * Purpose: Stores optimization profile definitions.
 * Why it exists: Export and quantization choices must be versioned and reusable across model releases.
 * Architecture fit: Reference registry for AI-6 artifact generation policy.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Optimization profile entity. */
@Entity
@Table(name = "optimization_profiles", schema = "optimization")
public class OptimizationProfileEntity {
    @Id private UUID id;
    private String profileKey;
    private String exportFormat;
    private String quantizationMode;
    private String precisionMode;
    private String targetRuntime;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String parametersJson;
    private Instant createdAt;

    protected OptimizationProfileEntity() {}

    /** Creates a profile. */
    public OptimizationProfileEntity(UUID id, String profileKey, String exportFormat, String quantizationMode, String precisionMode, String targetRuntime, String status, String parametersJson, Instant createdAt) {
        this.id = id; this.profileKey = profileKey; this.exportFormat = exportFormat; this.quantizationMode = quantizationMode; this.precisionMode = precisionMode; this.targetRuntime = targetRuntime; this.status = status; this.parametersJson = parametersJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getProfileKey() { return profileKey; }
}
