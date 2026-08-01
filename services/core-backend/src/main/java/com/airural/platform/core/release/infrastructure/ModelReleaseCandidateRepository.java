/*
 * Purpose: Persists release candidate workflow records.
 * Why it exists: Release candidates need validation and regression status tracking.
 * Architecture fit: JPA adapter for AI-10 release candidate process.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseCandidateEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for model release candidates. */
public interface ModelReleaseCandidateRepository extends JpaRepository<ReleaseCandidateEntity, UUID> {}
