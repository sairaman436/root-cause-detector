/*
 * Purpose: Provides persistence access to AI model registry records.
 * Why it exists: Gateway routing and model governance need durable model lookup.
 * Architecture fit: Repository adapter for the AI foundation model aggregate.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.AIModelEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for AI models. */
public interface AIModelRepository extends JpaRepository<AIModelEntity, UUID> {
    Optional<AIModelEntity> findByModelId(String modelId);
    boolean existsByModelId(String modelId);
}
