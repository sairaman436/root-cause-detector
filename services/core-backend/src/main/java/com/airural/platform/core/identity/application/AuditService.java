/*
 * Purpose: Writes audit events for identity actions.
 * Why it exists: Authentication and authorization workflows must be traceable for compliance.
 * Architecture fit: Implements the approved audit logging foundation.
 */
package com.airural.platform.core.identity.application;

import com.airural.platform.core.identity.domain.AuditEventEntity;
import com.airural.platform.core.identity.domain.AuditOutcome;
import com.airural.platform.core.identity.infrastructure.AuditEventRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Application service for audit event persistence. */
@Service
public class AuditService {
    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /** Persists an audit event in a separate transaction when possible. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID actorUserId, String eventType, AuditOutcome outcome, String ipAddress, String userAgent, String details) {
        auditEventRepository.save(new AuditEventEntity(actorUserId, eventType, outcome, ipAddress, userAgent, details));
    }
}
