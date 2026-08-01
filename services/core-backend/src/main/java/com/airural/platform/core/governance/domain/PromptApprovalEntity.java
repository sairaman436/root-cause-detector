/*
 * Purpose: Records prompt approval, rejection, and rollback decisions.
 * Why it exists: Production prompts require independent review, rationale, and approval-chain evidence.
 * Architecture fit: Approval workflow record for AI-9 prompt governance.
 */
package com.airural.platform.core.governance.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Prompt approval entity. */
@Entity
@Table(name = "prompt_approvals", schema = "governance")
public class PromptApprovalEntity {
    @Id private UUID id;
    private UUID promptId;
    private String decision;
    private String rationale;
    private UUID decidedBy;
    private String approvalChain;
    private Instant decidedAt;

    protected PromptApprovalEntity() {}

    /** Creates a prompt approval decision. */
    public PromptApprovalEntity(UUID id, UUID promptId, String decision, String rationale, UUID decidedBy, String approvalChain, Instant decidedAt) {
        this.id = id; this.promptId = promptId; this.decision = decision; this.rationale = rationale; this.decidedBy = decidedBy; this.approvalChain = approvalChain; this.decidedAt = decidedAt;
    }

    public UUID getId() { return id; }
    public String getDecision() { return decision; }
}
