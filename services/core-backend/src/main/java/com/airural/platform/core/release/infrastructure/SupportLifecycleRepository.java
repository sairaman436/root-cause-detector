/*
 * Purpose: Persists support lifecycle records.
 * Why it exists: Stable, hotfix, security patch, LTS, retirement, and upgrade policies must be queryable.
 * Architecture fit: JPA adapter for AI-10 support lifecycle.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.SupportLifecycleEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for support lifecycle records. */
public interface SupportLifecycleRepository extends JpaRepository<SupportLifecycleEntity, UUID> {}
