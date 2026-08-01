/*
 * Purpose: Stores release artifact metadata and integrity evidence.
 * Why it exists: GGUF, safetensors, Ollama, vLLM, Docker, research, development, production, and LTS artifacts require checksums and signatures.
 * Architecture fit: AI-10 artifact registry without building binary artifacts in the backend.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release artifact entity. */
@Entity
@Table(name = "release_artifacts", schema = "model_release")
public class ReleaseArtifactEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String artifactType;
    private String packageFormat;
    private String deploymentTarget;
    private String uri;
    private String checksumSha256;
    private String signature;
    private String sbomRef;
    private Long sizeBytes;
    private String status;
    private Instant createdAt;

    protected ReleaseArtifactEntity() {}

    /** Creates a release artifact. */
    public ReleaseArtifactEntity(UUID id, UUID releaseVersionId, String artifactType, String packageFormat, String deploymentTarget, String uri, String checksumSha256, String signature, String sbomRef, Long sizeBytes, String status, Instant createdAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.artifactType = artifactType; this.packageFormat = packageFormat; this.deploymentTarget = deploymentTarget; this.uri = uri; this.checksumSha256 = checksumSha256; this.signature = signature; this.sbomRef = sbomRef; this.sizeBytes = sizeBytes; this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getArtifactType() { return artifactType; }
    public String getPackageFormat() { return packageFormat; }
    public String getDeploymentTarget() { return deploymentTarget; }
    public String getChecksumSha256() { return checksumSha256; }
    public String getStatus() { return status; }
}
