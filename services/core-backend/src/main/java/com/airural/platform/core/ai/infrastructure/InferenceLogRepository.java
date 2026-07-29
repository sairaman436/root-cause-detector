/*
 * Purpose: Provides persistence access to inference logs.
 * Why it exists: Operators need inference history, latency, and safety telemetry.
 * Architecture fit: Repository adapter for AI gateway observability.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.InferenceLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for inference logs. */
public interface InferenceLogRepository extends JpaRepository<InferenceLogEntity, UUID> {
}
