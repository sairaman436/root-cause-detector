/* Purpose: Persists future training candidates. Why it exists: Dataset candidates need approval state, quality score, and lineage. Architecture fit: JPA registry adapter for AI-7. */
package com.airural.platform.core.learning.infrastructure;

import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** JPA repository for training candidates. */
public interface TrainingCandidateRepository extends JpaRepository<TrainingCandidateEntity, UUID> {
    /** Lists candidates for reviewer work queues. */
    List<TrainingCandidateEntity> findByApprovalStatusOrderByCreatedAtAsc(String approvalStatus);
}
