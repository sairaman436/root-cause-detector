/*
 * Purpose: Persists evaluation dataset records.
 * Why it exists: Model and RAG evaluation require stable, approved datasets separated from training inputs.
 * Architecture fit: Infrastructure adapter for AI evaluation governance.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.EvaluationDatasetEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for evaluation dataset records. */
public interface EvaluationDatasetRepository extends JpaRepository<EvaluationDatasetEntity, UUID> {
}
