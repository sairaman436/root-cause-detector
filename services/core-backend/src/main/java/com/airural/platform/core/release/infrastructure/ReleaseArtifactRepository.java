/*
 * Purpose: Persists release artifact records.
 * Why it exists: Release APIs must expose artifacts, packages, checksums, signatures, and SBOM references.
 * Architecture fit: JPA adapter for AI-10 artifact registry.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseArtifactEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for release artifacts. */
public interface ReleaseArtifactRepository extends JpaRepository<ReleaseArtifactEntity, UUID> {
    List<ReleaseArtifactEntity> findByReleaseVersionId(UUID releaseVersionId);
}
