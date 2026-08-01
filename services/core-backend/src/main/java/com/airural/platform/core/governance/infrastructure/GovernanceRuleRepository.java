/*
 * Purpose: Persists governance rules.
 * Why it exists: Rule lists are evaluated and reported separately from policy metadata.
 * Architecture fit: JPA adapter for AI-9 policy rules.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.GovernanceRuleEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for governance rules. */
public interface GovernanceRuleRepository extends JpaRepository<GovernanceRuleEntity, UUID> {
    List<GovernanceRuleEntity> findByPolicyId(UUID policyId);
}
