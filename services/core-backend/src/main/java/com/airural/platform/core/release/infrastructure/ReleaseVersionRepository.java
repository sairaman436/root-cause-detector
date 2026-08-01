/*
 * Purpose: Persists release version records.
 * Why it exists: APIs need latest, history, and semantic-version lookups.
 * Architecture fit: JPA adapter for the AI-10 release version aggregate.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseVersionEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for release versions. */
public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersionEntity, UUID> {
    Optional<ReleaseVersionEntity> findFirstByLifecycleStatusOrderByReleasedAtDesc(String lifecycleStatus);
    Optional<ReleaseVersionEntity> findBySemanticVersion(String semanticVersion);
}
