/*
 * Purpose: Persists policy violation records.
 * Why it exists: Governance observability needs violation counts and remediation tracking.
 * Architecture fit: JPA adapter for AI-9 policy monitoring.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.PolicyViolationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for policy violations. */
public interface PolicyViolationRepository extends JpaRepository<PolicyViolationEntity, UUID> {
    long countByStatusNot(String status);
}
