/*
 * Purpose: Persists review board approval records.
 * Why it exists: AI-4 requires explicit final review evidence from internal and external boards.
 * Architecture fit: Infrastructure adapter for fine-tuning governance approvals.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.TrainingApprovalEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for fine-tuning approvals. */
public interface TrainingApprovalRepository extends JpaRepository<TrainingApprovalEntity, UUID> {
}
