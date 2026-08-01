/*
 * Purpose: Stores deployment package metadata for optimized artifacts.
 * Why it exists: AI-6 must prepare deployable packages for Ollama, vLLM, llama.cpp, Docker, Kubernetes, offline, server, workstation, and research targets.
 * Architecture fit: Deployment package registry linked to optimization runs.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Deployment package entity. */
@Entity
@Table(name = "deployment_packages", schema = "optimization")
public class DeploymentPackageEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private String packageType;
    private String targetEnvironment;
    private String packageUri;
    private String manifestType;
    private String checksumSha256;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String manifestJson;
    private Instant createdAt;

    protected DeploymentPackageEntity() {}

    /** Creates a deployment package. */
    public DeploymentPackageEntity(UUID id, UUID optimizationRunId, String packageType, String targetEnvironment, String packageUri, String manifestType, String checksumSha256, String status, String manifestJson, Instant createdAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.packageType = packageType; this.targetEnvironment = targetEnvironment; this.packageUri = packageUri; this.manifestType = manifestType; this.checksumSha256 = checksumSha256; this.status = status; this.manifestJson = manifestJson; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getPackageType() { return packageType; }
    public String getTargetEnvironment() { return targetEnvironment; }
    public String getStatus() { return status; }
}
