/*
 * Purpose: Provides persistence access to evidence version history.
 * Why it exists: Evidence metadata changes must expose auditable version history.
 * Architecture fit: Infrastructure repository for immutable evidence versions.
 */
package com.airural.platform.core.evidence.infrastructure;

import com.airural.platform.core.evidence.domain.EvidenceVersionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for evidence versions. */
public interface EvidenceVersionRepository extends JpaRepository<EvidenceVersionEntity, UUID> {
    List<EvidenceVersionEntity> findByEvidence_IdOrderByVersionNumberDesc(UUID evidenceId);
}
