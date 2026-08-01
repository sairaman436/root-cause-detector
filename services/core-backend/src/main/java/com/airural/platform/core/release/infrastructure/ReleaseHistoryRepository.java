/*
 * Purpose: Persists release lifecycle history.
 * Why it exists: Promotion and rollback need an immutable release audit trail.
 * Architecture fit: JPA adapter for AI-10 release history.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseHistoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for release history. */
public interface ReleaseHistoryRepository extends JpaRepository<ReleaseHistoryEntity, UUID> {}
