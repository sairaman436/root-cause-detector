/*
 * Purpose: Persists model cards for fine-tuned adapters.
 * Why it exists: Intended use, limitations, safety notes, and license metadata must be auditable.
 * Architecture fit: Infrastructure adapter for AI-4 model card deliverables.
 */
package com.airural.platform.core.finetuning.infrastructure;

import com.airural.platform.core.finetuning.domain.ModelCardEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for model cards. */
public interface ModelCardRepository extends JpaRepository<ModelCardEntity, UUID> {
}
