/*
 * Purpose: Provides persistence access to recommendation evidence links.
 * Why it exists: Recommendations must expose supporting survey, document, policy, and case evidence.
 * Architecture fit: Repository adapter for recommendation explainability.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.RecommendationEvidenceEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for recommendation evidence. */
public interface RecommendationEvidenceRepository extends JpaRepository<RecommendationEvidenceEntity, UUID> {}
