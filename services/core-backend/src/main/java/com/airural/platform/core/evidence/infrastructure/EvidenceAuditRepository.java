/*
 * Purpose: Provides persistence access to evidence audit events.
 * Why it exists: Evidence lifecycle operations need module-local audit trails.
 * Architecture fit: Infrastructure repository for evidence audit records.
 */
package com.airural.platform.core.evidence.infrastructure;

import com.airural.platform.core.evidence.domain.EvidenceAuditEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for evidence audit events. */
public interface EvidenceAuditRepository extends JpaRepository<EvidenceAuditEntity, UUID> {
    List<EvidenceAuditEntity> findByEvidence_IdOrderByCreatedAtAsc(UUID evidenceId);
}
