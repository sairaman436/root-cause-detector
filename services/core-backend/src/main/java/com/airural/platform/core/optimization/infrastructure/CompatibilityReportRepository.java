/*
 * Purpose: Persists artifact compatibility reports.
 * Why it exists: Release decisions require runtime and hardware compatibility evidence.
 * Architecture fit: JPA validation evidence repository.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.CompatibilityReportEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for compatibility reports. */
public interface CompatibilityReportRepository extends JpaRepository<CompatibilityReportEntity, UUID> {}
