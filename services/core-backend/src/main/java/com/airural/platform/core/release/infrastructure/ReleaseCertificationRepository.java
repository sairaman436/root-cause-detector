/*
 * Purpose: Persists release certification evidence.
 * Why it exists: Production releases cannot proceed unless all certification gates pass.
 * Architecture fit: JPA adapter for AI-10 certification records.
 */
package com.airural.platform.core.release.infrastructure;

import com.airural.platform.core.release.domain.ReleaseCertificationEntity;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for release certifications. */
public interface ReleaseCertificationRepository extends JpaRepository<ReleaseCertificationEntity, UUID> {
    List<ReleaseCertificationEntity> findByReleaseVersionId(UUID releaseVersionId);
}
