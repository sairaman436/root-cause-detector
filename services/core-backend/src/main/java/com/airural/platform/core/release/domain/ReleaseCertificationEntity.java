/*
 * Purpose: Stores certification results for release quality gates.
 * Why it exists: Accuracy, reasoning, policy compliance, safety, performance, security, hallucination, citation, latency, memory, and resource usage must be independently verified.
 * Architecture fit: AI-10 certification evidence entity.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release certification entity. */
@Entity
@Table(name = "release_certifications", schema = "model_release")
public class ReleaseCertificationEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String certificationType;
    private String board;
    private Double score;
    private String status;
    private String evidenceRef;
    private Instant certifiedAt;

    protected ReleaseCertificationEntity() {}

    /** Creates a release certification. */
    public ReleaseCertificationEntity(UUID id, UUID releaseVersionId, String certificationType, String board, Double score, String status, String evidenceRef, Instant certifiedAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.certificationType = certificationType; this.board = board; this.score = score; this.status = status; this.evidenceRef = evidenceRef; this.certifiedAt = certifiedAt;
    }

    public String getStatus() { return status; }
}
