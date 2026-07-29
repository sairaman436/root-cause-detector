/*
 * Purpose: Provides persistence access to audit events.
 * Why it exists: Identity actions must be recorded for security monitoring and compliance.
 * Architecture fit: Infrastructure adapter for audit logging.
 */
package com.airural.platform.core.identity.infrastructure;

import com.airural.platform.core.identity.domain.AuditEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for audit events. */
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
}
