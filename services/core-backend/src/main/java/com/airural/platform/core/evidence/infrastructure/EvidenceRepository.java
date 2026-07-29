/*
 * Purpose: Provides persistence access for evidence aggregate roots.
 * Why it exists: Evidence services need CRUD, duplicate detection, and specification-backed search.
 * Architecture fit: Infrastructure repository for the Evidence module.
 */
package com.airural.platform.core.evidence.infrastructure;

import com.airural.platform.core.evidence.domain.EvidenceEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Spring Data repository for evidence assets. */
public interface EvidenceRepository extends JpaRepository<EvidenceEntity, UUID>, JpaSpecificationExecutor<EvidenceEntity> {
    @EntityGraph(attributePaths = {"metadata", "tags"})
    Optional<EvidenceEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"metadata", "tags"})
    Optional<EvidenceEntity> findByIdAndIsActiveTrue(UUID id);

    boolean existsByOrganizationIdAndSha256ChecksumAndIsActiveTrue(UUID organizationId, String sha256Checksum);
}
