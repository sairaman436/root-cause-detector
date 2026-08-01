/*
 * Purpose: Persists release board approval decisions.
 * Why it exists: Architecture, research, security, performance, governance, audit, and release boards must approve production releases.
 * Architecture fit: JPA adapter for AI-10 approval evidence.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseApprovalEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for release approvals. */
public interface ReleaseApprovalRepository extends JpaRepository<ReleaseApprovalEntity, UUID> {
    List<ReleaseApprovalEntity> findByReleaseVersionId(UUID releaseVersionId);
}
