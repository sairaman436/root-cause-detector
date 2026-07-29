/*
 * Purpose: Provides persistence access to token usage records.
 * Why it exists: Cost and rate governance depend on durable token accounting.
 * Architecture fit: Repository adapter for AI usage telemetry.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.TokenUsageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for token usage records. */
public interface TokenUsageRepository extends JpaRepository<TokenUsageEntity, UUID> {
}
