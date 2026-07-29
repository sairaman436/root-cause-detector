/*
 * Purpose: Provides persistence access to model versions.
 * Why it exists: Model serving routes concrete versions with capability and resource metadata.
 * Architecture fit: Repository adapter for model registry version records.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.ModelVersionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for model versions. */
public interface ModelVersionRepository extends JpaRepository<ModelVersionEntity, UUID> {
}
