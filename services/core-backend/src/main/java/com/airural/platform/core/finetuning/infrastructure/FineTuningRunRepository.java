/*
 * Purpose: Persists supervised fine-tuning run records.
 * Why it exists: Fine-tuning jobs, rollback, reports, and model listings need durable run state.
 * Architecture fit: Infrastructure adapter for AI-4 run lifecycle.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.FineTuningRunEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for fine-tuning runs. */
public interface FineTuningRunRepository extends JpaRepository<FineTuningRunEntity, UUID> {
}
