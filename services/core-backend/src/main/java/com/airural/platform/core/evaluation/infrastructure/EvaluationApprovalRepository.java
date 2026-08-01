/*
 * Purpose: Persists independent review board decisions.
 * Why it exists: Promotion/rejection decisions must retain complete evaluation board history.
 * Architecture fit: Infrastructure adapter for AI-5 governance approvals.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.EvaluationApprovalEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for evaluation approvals. */
public interface EvaluationApprovalRepository extends JpaRepository<EvaluationApprovalEntity, UUID> {
}
