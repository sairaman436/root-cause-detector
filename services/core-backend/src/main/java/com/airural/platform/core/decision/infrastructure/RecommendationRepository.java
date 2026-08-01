/*
 * Purpose: Provides persistence access to recommendations.
 * Why it exists: Recommendation and explanation APIs need ranked recommendation records.
 * Architecture fit: Repository adapter for recommendation records.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.RecommendationEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for recommendations. */
public interface RecommendationRepository extends JpaRepository<RecommendationEntity, UUID> {
    List<RecommendationEntity> findByDecisionIdOrderByPriorityAsc(UUID decisionId);
}
