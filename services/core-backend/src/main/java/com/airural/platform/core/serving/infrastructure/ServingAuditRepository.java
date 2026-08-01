/* Purpose: Persists serving audit events. Why it exists: Inference traffic requires immutable audit history. Architecture fit: JPA adapter for serving governance evidence. */
package com.airural.platform.core.serving.infrastructure;

import com.airural.platform.core.serving.domain.ServingAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for serving audits. */
public interface ServingAuditRepository extends JpaRepository<ServingAuditEntity, UUID> {}
