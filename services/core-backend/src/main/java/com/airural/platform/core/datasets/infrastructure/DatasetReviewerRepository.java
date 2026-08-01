/*
 * Purpose: Persists reviewer assignments and reviewer state.
 * Why it exists: The annotation platform needs explicit ownership for approval and quality review.
 * Architecture fit: Infrastructure adapter for dataset human-review workflows.
 */
package com.airural.platform.core.datasets.infrastructure;

import com.airural.platform.core.datasets.domain.DatasetReviewerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for dataset reviewers. */
public interface DatasetReviewerRepository extends JpaRepository<DatasetReviewerEntity, UUID> {
}
