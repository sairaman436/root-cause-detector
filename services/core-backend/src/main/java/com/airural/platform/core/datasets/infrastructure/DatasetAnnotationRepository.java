/*
 * Purpose: Persists human and expert annotation decisions.
 * Why it exists: Fine-tuning and evaluation datasets require auditable labels and conflict resolution.
 * Architecture fit: Infrastructure adapter for dataset annotation governance.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetAnnotationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset annotations. */
public interface DatasetAnnotationRepository extends JpaRepository<DatasetAnnotationEntity, UUID> {
}
