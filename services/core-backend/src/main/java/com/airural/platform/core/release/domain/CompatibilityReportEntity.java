/*
 * Purpose: Stores release compatibility validation results.
 * Why it exists: Model artifacts must be validated across Ollama, vLLM, llama.cpp, operating systems, CPU/GPU, cloud, and air-gapped targets.
 * Architecture fit: AI-10 compatibility matrix entity.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Compatibility report entity. */
@Entity(name = "ModelReleaseCompatibilityReportEntity")
@Table(name = "compatibility_reports", schema = "model_release")
public class CompatibilityReportEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String platform;
    private String runtime;
    private String hardwareProfile;
    private String status;
    private String notes;
    private Instant validatedAt;

    protected CompatibilityReportEntity() {}

    /** Creates a compatibility report. */
    public CompatibilityReportEntity(UUID id, UUID releaseVersionId, String platform, String runtime, String hardwareProfile, String status, String notes, Instant validatedAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.platform = platform; this.runtime = runtime; this.hardwareProfile = hardwareProfile; this.status = status; this.notes = notes; this.validatedAt = validatedAt;
    }

    public UUID getId() { return id; }
    public String getPlatform() { return platform; }
    public String getRuntime() { return runtime; }
    public String getHardwareProfile() { return hardwareProfile; }
    public String getStatus() { return status; }
}
