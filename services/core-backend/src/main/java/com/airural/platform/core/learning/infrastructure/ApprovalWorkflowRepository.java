/* Purpose: Persists approval workflow records. Why it exists: Every learning candidate approval/rejection must be board-reviewed. Architecture fit: JPA governance adapter. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.ApprovalWorkflowEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for approval workflows. */
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflowEntity, UUID> {}
