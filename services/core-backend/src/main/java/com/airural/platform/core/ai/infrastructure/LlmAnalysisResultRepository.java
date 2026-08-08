/*
 * Purpose: Provides persistence access to local LLM analysis results.
 * Why it exists: AI application services need durable storage and retrieval for validated root-cause outputs and failed operational attempts.
 * Architecture fit: Infrastructure adapter for the AI analysis aggregate.
 */
package com.airural.platform.core.ai.infrastructure;

import com.airural.platform.core.ai.domain.LlmAnalysisResultEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for local LLM analysis results. */
public interface LlmAnalysisResultRepository extends JpaRepository<LlmAnalysisResultEntity, UUID> {
}
