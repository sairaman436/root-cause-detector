/*
 * Purpose: Persists artifact signature evidence.
 * Why it exists: Integrity, checksum, tamper, and license controls must be auditable.
 * Architecture fit: JPA repository for AI-6 security evidence.
 */
package com.airural.platform.core.optimization.infrastructure;

import com.airural.platform.core.optimization.domain.ArtifactSignatureEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for artifact signatures. */
public interface ArtifactSignatureRepository extends JpaRepository<ArtifactSignatureEntity, UUID> {}
