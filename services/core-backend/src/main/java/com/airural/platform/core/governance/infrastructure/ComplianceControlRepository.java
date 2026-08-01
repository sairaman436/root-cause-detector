/*
 * Purpose: Persists compliance framework mappings.
 * Why it exists: Governance APIs need current control status and compliance score evidence.
 * Architecture fit: JPA adapter for AI-9 compliance matrix records.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.ComplianceControlEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for compliance controls. */
public interface ComplianceControlRepository extends JpaRepository<ComplianceControlEntity, UUID> {}
