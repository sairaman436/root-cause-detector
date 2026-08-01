/*
 * Purpose: Persists governance policy records.
 * Why it exists: Policy APIs and approval workflows need durable policy lookup.
 * Architecture fit: JPA adapter for the AI-9 governance policy aggregate.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.GovernancePolicyEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for governance policies. */
public interface GovernancePolicyRepository extends JpaRepository<GovernancePolicyEntity, UUID> {
    Optional<GovernancePolicyEntity> findByPolicyKey(String policyKey);
}
