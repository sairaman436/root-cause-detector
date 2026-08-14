/*
 * Purpose: Provides persistence access to server-backed multimodal reviews.
 * Why it exists: Review progress, duplicate protection, and dashboard aggregation require durable queries.
 * Architecture fit: JPA adapter for the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.MultimodalHumanReviewEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for multimodal human review records. */
public interface MultimodalHumanReviewRepository extends JpaRepository<MultimodalHumanReviewEntity, UUID> {
    List<MultimodalHumanReviewEntity> findByTraceId(String traceId);
    boolean existsByTraceIdAndReviewerId(String traceId, UUID reviewerId);
}

