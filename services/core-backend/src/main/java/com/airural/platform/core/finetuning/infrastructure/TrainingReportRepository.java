/*
 * Purpose: Persists fine-tuning report artifacts.
 * Why it exists: Training, evaluation, benchmark, loss curve, and audit reports need durable artifact metadata.
 * Architecture fit: Infrastructure adapter for AI-4 report deliverables.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.TrainingReportEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training reports. */
public interface TrainingReportRepository extends JpaRepository<TrainingReportEntity, UUID> {
}
