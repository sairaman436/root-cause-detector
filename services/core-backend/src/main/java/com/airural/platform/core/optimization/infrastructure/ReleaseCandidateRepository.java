/*
 * Purpose: Persists release candidate records.
 * Why it exists: Promotion decisions must be explicit, reviewable, and separate from deployment execution.
 * Architecture fit: JPA repository for AI-6 release governance.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.ReleaseCandidateEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for release candidates. */
public interface ReleaseCandidateRepository extends JpaRepository<ReleaseCandidateEntity, UUID> {}
