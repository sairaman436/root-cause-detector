/*
 * Purpose: Provides persistence access to confidence score records.
 * Why it exists: Confidence APIs need score component lookup.
 * Architecture fit: Repository adapter for confidence engine outputs.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.ConfidenceScoreEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for confidence scores. */
public interface ConfidenceScoreRepository extends JpaRepository<ConfidenceScoreEntity, UUID> {
    Optional<ConfidenceScoreEntity> findByDecisionId(UUID decisionId);
}
