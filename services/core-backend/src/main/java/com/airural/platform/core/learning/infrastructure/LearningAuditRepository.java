/* Purpose: Persists immutable learning audit events. Why it exists: Sensitive learning operations must be traceable and tamper-evident. Architecture fit: JPA audit adapter for AI-7. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.LearningAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for learning audits. */
public interface LearningAuditRepository extends JpaRepository<LearningAuditEntity, UUID> {}
