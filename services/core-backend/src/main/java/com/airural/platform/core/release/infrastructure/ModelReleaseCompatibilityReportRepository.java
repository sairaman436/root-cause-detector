/*
 * Purpose: Persists compatibility matrix records.
 * Why it exists: Enterprise operators need supported runtime, OS, hardware, cloud, and air-gapped target evidence.
 * Architecture fit: JPA adapter for AI-10 compatibility reports.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.CompatibilityReportEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for model release compatibility reports. */
public interface ModelReleaseCompatibilityReportRepository extends JpaRepository<CompatibilityReportEntity, UUID> {
    List<CompatibilityReportEntity> findByReleaseVersionId(UUID releaseVersionId);
}
