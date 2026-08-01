/*
 * Purpose: Persists release observability metrics.
 * Why it exists: Downloads, deployments, failures, rollback rate, compatibility, and adoption need durable snapshots.
 * Architecture fit: JPA adapter for AI-10 release observability.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseMetricsEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for release metrics. */
public interface ReleaseMetricsRepository extends JpaRepository<ReleaseMetricsEntity, UUID> {}
