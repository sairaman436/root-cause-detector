/*
 * Purpose: Provides persistence access to reasoning traces.
 * Why it exists: Explainability APIs and audits need persisted reasoning steps.
 * Architecture fit: Repository adapter for agent reasoning trace records.
 */
package com.airural.platform.core.agents.infrastructure;

import com.airural.platform.core.agents.domain.ReasoningTraceEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for reasoning traces. */
public interface ReasoningTraceRepository extends JpaRepository<ReasoningTraceEntity, UUID> {
    List<ReasoningTraceEntity> findByExecutionIdOrderByIdAsc(UUID executionId);
}
