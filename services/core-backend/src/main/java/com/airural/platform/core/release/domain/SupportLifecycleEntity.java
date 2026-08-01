/*
 * Purpose: Stores support lifecycle policy for a release.
 * Why it exists: Stable, hotfix, security patch, LTS, retirement, and upgrade paths must be explicit for enterprise operators.
 * Architecture fit: AI-10 support lifecycle entity.
 */
package com.airural.platform.core.release.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Support lifecycle entity. */
@Entity
@Table(name = "support_lifecycle", schema = "model_release")
public class SupportLifecycleEntity {
    @Id private UUID id;
    private UUID releaseVersionId;
    private String supportTier;
    private Instant supportStart;
    private Instant supportEnd;
    private String retirementPolicy;
    private String upgradePath;
    private String securityPatchPolicy;
    private Instant createdAt;

    protected SupportLifecycleEntity() {}

    /** Creates a support lifecycle record. */
    public SupportLifecycleEntity(UUID id, UUID releaseVersionId, String supportTier, Instant supportStart, Instant supportEnd, String retirementPolicy, String upgradePath, String securityPatchPolicy, Instant createdAt) {
        this.id = id; this.releaseVersionId = releaseVersionId; this.supportTier = supportTier; this.supportStart = supportStart; this.supportEnd = supportEnd; this.retirementPolicy = retirementPolicy; this.upgradePath = upgradePath; this.securityPatchPolicy = securityPatchPolicy; this.createdAt = createdAt;
    }
}
