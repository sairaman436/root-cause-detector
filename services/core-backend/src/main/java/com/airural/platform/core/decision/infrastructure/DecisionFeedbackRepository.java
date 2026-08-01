/*
 * Purpose: Provides persistence access to decision feedback.
 * Why it exists: Human feedback, overrides, and learning datasets need durable records.
 * Architecture fit: Repository adapter for decision memory.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.DecisionFeedbackEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for decision feedback. */
public interface DecisionFeedbackRepository extends JpaRepository<DecisionFeedbackEntity, UUID> {}
