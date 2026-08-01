/*
 * Purpose: Stores checksum and signature evidence for optimized artifacts.
 * Why it exists: Artifact signing, tamper detection, checksum verification, and license validation are mandatory release controls.
 * Architecture fit: Security evidence entity for AI-6 artifacts.
 */
package com.airural.platform.core.optimization.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Artifact signature entity. */
@Entity
@Table(name = "artifact_signatures", schema = "optimization")
public class ArtifactSignatureEntity {
    @Id private UUID id;
    private UUID optimizationRunId;
    private UUID artifactId;
    private String checksumSha256;
    private String signatureAlgorithm;
    private String signatureValue;
    private String signer;
    private String integrityStatus;
    private String licenseStatus;
    @Column(columnDefinition = "TEXT")
    private String tamperEvidenceJson;
    private Instant signedAt;

    protected ArtifactSignatureEntity() {}

    /** Creates an artifact signature. */
    public ArtifactSignatureEntity(UUID id, UUID optimizationRunId, UUID artifactId, String checksumSha256, String signatureAlgorithm, String signatureValue, String signer, String integrityStatus, String licenseStatus, String tamperEvidenceJson, Instant signedAt) {
        this.id = id; this.optimizationRunId = optimizationRunId; this.artifactId = artifactId; this.checksumSha256 = checksumSha256; this.signatureAlgorithm = signatureAlgorithm; this.signatureValue = signatureValue; this.signer = signer; this.integrityStatus = integrityStatus; this.licenseStatus = licenseStatus; this.tamperEvidenceJson = tamperEvidenceJson; this.signedAt = signedAt;
    }
}
