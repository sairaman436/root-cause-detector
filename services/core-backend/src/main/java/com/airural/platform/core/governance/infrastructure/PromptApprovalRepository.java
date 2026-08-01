/*
 * Purpose: Persists prompt approval decisions.
 * Why it exists: Prompt governance requires durable reviewer decisions and approval chains.
 * Architecture fit: JPA adapter for AI-9 approval workflow records.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.PromptApprovalEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for prompt approvals. */
public interface PromptApprovalRepository extends JpaRepository<PromptApprovalEntity, UUID> {}
