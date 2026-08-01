/*
 * Purpose: Persists document fingerprints.
 * Why it exists: Incremental acquisition needs duplicate and unchanged-document detection.
 * Architecture fit: Infrastructure adapter for AI-2 fingerprinting.
 */
package com.airural.platform.core.knowledge.infrastructure;

import com.airural.platform.core.knowledge.domain.KnowledgeFingerprintEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for knowledge fingerprints. */
public interface KnowledgeFingerprintRepository extends JpaRepository<KnowledgeFingerprintEntity, UUID> {
    boolean existsByFingerprint(String fingerprint);
}
