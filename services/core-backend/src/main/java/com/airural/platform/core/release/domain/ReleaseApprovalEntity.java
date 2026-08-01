/*
 * Purpose: Stores board approval decisions for model releases.
 * Why it exists: Production release requires architecture, AI research, security, performance, governance, external audit, and release board approvals.
 * Architecture fit: AI-10 approval chain entity.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Release approval entity. */
@Entity
@Table(name = "release_approvals", schema = "model_release")
public class ReleaseApprovalEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String board;
    private String decision;
    private String rationale;
    private UUID approvedBy;
    private Instant decidedAt;

    protected ReleaseApprovalEntity() {}

    /** Creates a release approval. */
    public ReleaseApprovalEntity(UUID id, UUID releaseVersionId, String board, String decision, String rationale, UUID approvedBy, Instant decidedAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.board = board; this.decision = decision; this.rationale = rationale; this.approvedBy = approvedBy; this.decidedAt = decidedAt;
    }

    public String getDecision() { return decision; }
}
