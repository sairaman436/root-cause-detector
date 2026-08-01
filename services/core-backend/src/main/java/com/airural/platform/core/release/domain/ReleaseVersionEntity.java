/*
 * Purpose: Stores official Rural Intelligence Foundation Model release versions.
 * Why it exists: Enterprise model releases require semantic version, lifecycle status, channel, LTS, and model-card metadata.
 * Architecture fit: Root release engineering aggregate for AI-10.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release version entity. */
@Entity
@Table(name = "release_versions", schema = "model_release")
public class ReleaseVersionEntity {
    @Id private UUID id;
    private String modelName;
    private String semanticVersion;
    private String releaseChannel;
    private String lifecycleStatus;
    private Boolean lts;
    private String modelCardJson;
    private String releaseNotes;
    private String license;
    private Instant releasedAt;
    private Instant createdAt;

    protected ReleaseVersionEntity() {}

    /** Creates a release version. */
    public ReleaseVersionEntity(UUID id, String modelName, String semanticVersion, String releaseChannel, String lifecycleStatus, Boolean lts, String modelCardJson, String releaseNotes, String license, Instant releasedAt, Instant createdAt) {
        this.id = id; this.modelName = modelName; this.semanticVersion = semanticVersion; this.releaseChannel = releaseChannel; this.lifecycleStatus = lifecycleStatus; this.lts = lts; this.modelCardJson = modelCardJson; this.releaseNotes = releaseNotes; this.license = license; this.releasedAt = releasedAt; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getModelName() { return modelName; }
    public String getSemanticVersion() { return semanticVersion; }
    public String getReleaseChannel() { return releaseChannel; }
    public String getLifecycleStatus() { return lifecycleStatus; }
    public Boolean getLts() { return lts; }
    public String getModelCardJson() { return modelCardJson; }
}
