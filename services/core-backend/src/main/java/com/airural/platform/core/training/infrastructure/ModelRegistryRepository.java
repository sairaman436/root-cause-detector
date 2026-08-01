/*
 * Purpose: Persists training model registry metadata.
 * Why it exists: Future deployments require governed model lineage and license metadata before serving.
 * Architecture fit: Infrastructure adapter for AI-3 model registry.
 */
package com.airural.platform.core.training.infrastructure;

import com.airural.platform.core.training.domain.ModelRegistryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for model registry records. */
public interface ModelRegistryRepository extends JpaRepository<ModelRegistryEntity, UUID> {
}
