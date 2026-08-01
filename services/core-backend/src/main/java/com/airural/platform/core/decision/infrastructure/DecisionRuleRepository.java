/*
 * Purpose: Provides persistence access to decision rules.
 * Why it exists: Rule engine evaluates active policy, eligibility, constraint, and priority rules.
 * Architecture fit: Repository adapter for configurable decision rules.
 */
package com.airural.platform.core.decision.infrastructure;

import com.airural.platform.core.decision.domain.DecisionRuleEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for decision rules. */
public interface DecisionRuleRepository extends JpaRepository<DecisionRuleEntity, UUID> {
    List<DecisionRuleEntity> findByStatusOrderByPriorityAsc(String status);
}
