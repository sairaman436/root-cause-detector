/*
 * Purpose: Persists artifact risk assessments.
 * Why it exists: Approvals require current residual risk evidence.
 * Architecture fit: JPA adapter for AI-9 risk review records.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.RiskAssessmentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for risk assessments. */
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {}
