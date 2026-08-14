/*
 * Purpose: Provides persistence access to authenticated human-quality reviews.
 * Why it exists: Review state and duplicate protection belong to the evaluation storage boundary.
 * Architecture fit: JPA adapter for the human evaluation workflow.
 */
package com.airural.platform.core.evaluation.infrastructure;

import com.airural.platform.core.evaluation.domain.HumanEvaluationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for human evaluation records. */
public interface HumanEvaluationRepository extends JpaRepository<HumanEvaluationEntity, UUID> {
    List<HumanEvaluationEntity> findByEvaluationSetVersion(String evaluationSetVersion);
    boolean existsByEvaluationSetVersionAndExampleIdAndReviewerId(String evaluationSetVersion, String exampleId, UUID reviewerId);
}

