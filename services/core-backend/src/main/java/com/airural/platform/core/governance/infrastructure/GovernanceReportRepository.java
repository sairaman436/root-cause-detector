/*
 * Purpose: Persists governance board reports.
 * Why it exists: Release and external audit boards need durable report snapshots.
 * Architecture fit: JPA adapter for AI-9 governance reporting records.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.GovernanceReportEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for governance reports. */
public interface GovernanceReportRepository extends JpaRepository<GovernanceReportEntity, UUID> {}
