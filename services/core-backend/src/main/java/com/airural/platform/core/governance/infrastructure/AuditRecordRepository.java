/*
 * Purpose: Persists immutable governance audit records.
 * Why it exists: Governance audit APIs and tamper detection require durable event hashes.
 * Architecture fit: JPA adapter for AI-9 immutable audit ledger.
 */
package com.airural.platform.core.governance.infrastructure;

import com.airural.platform.core.governance.domain.AuditRecordEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for governance audit records. */
public interface AuditRecordRepository extends JpaRepository<AuditRecordEntity, UUID> {
    Optional<AuditRecordEntity> findTopByOrderByCreatedAtDesc();
}
