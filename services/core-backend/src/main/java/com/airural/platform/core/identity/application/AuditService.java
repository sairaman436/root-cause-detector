/*
 * Purpose: Writes audit events for identity actions.
 * Why it exists: Authentication and authorization workflows must be traceable for compliance.
 * Architecture fit: Implements the approved audit logging foundation.
 */
package com.airural.platform.core.identity.application;

import com.airural.platform.core.events.application.OutboxService;
import com.airural.platform.core.identity.domain.AuditEventEntity;
import com.airural.platform.core.identity.domain.AuditOutcome;
import com.airural.platform.core.identity.infrastructure.AuditEventRepository;
import com.airural.platform.shared.events.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Application service for audit event persistence. */
@Service
public class AuditService {
    private final AuditEventRepository auditEventRepository;
    private final OutboxService outboxService;

    public AuditService(AuditEventRepository auditEventRepository, OutboxService outboxService) {
        this.auditEventRepository = auditEventRepository;
        this.outboxService = outboxService;
    }

    /** Persists an audit event in a separate transaction when possible. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID actorUserId, String eventType, AuditOutcome outcome, String ipAddress, String userAgent, String details) {
        AuditEventEntity audit = auditEventRepository.save(new AuditEventEntity(actorUserId, eventType, outcome, ipAddress, userAgent, details));
        outboxService.enqueue(
                EventTopic.AUDIT_CREATED,
                "AUDIT",
                audit.id(),
                null,
                actorUserId,
                new EventPayloads.AuditPayload(audit.id(), actorUserId, audit.eventType(), audit.outcome().name(), audit.createdAt()));
    }
}
