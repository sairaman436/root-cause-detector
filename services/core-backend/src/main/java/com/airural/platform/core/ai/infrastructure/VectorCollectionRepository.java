/*
 * Purpose: Provides persistence access to vector collection metadata.
 * Why it exists: Qdrant collection state is governed through the platform catalog.
 * Architecture fit: Repository adapter for vector database metadata.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.VectorCollectionEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for vector collections. */
public interface VectorCollectionRepository extends JpaRepository<VectorCollectionEntity, UUID> {
    Optional<VectorCollectionEntity> findByName(String name);
}
