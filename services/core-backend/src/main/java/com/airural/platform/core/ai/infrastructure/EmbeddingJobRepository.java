/*
 * Purpose: Provides persistence access to embedding jobs.
 * Why it exists: Embedding queue state must be tracked for reprocessing and observability.
 * Architecture fit: Repository adapter for embedding pipeline jobs.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.EmbeddingJobEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for embedding jobs. */
public interface EmbeddingJobRepository extends JpaRepository<EmbeddingJobEntity, UUID> {
}
