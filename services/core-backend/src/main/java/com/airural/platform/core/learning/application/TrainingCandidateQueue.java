/*
 * Purpose: Defines the application boundary for queueing governed training candidates.
 * Why it exists: Evaluation orchestration must depend on a small use-case contract rather than the full learning service.
 * Architecture fit: Dependency-inversion port between the evaluation and learning bounded contexts.
 */
package com.airural.platform.core.learning.application;

import com.airural.platform.core.learning.domain.TrainingCandidateEntity;
import java.math.BigDecimal;
import java.util.UUID;

/** Port used to place eligible evaluation results into the existing review queue. */
public interface TrainingCandidateQueue {
    /** Immutable data contract transferred from evaluation to governed learning. */
    record EvaluationCandidateData(
            UUID evaluationResultId,
            String taskType,
            String scenarioGroup,
            String input,
            String retrievedContext,
            String aiOutput,
            String evidenceUsedJson,
            String sourceType,
            String modelVersion,
            String promptVersion,
            BigDecimal evaluationScore,
            String evaluationMetadataJson,
            boolean synthetic) {}

    /** Queues a candidate as pending human review; it never approves or trains. */
    TrainingCandidateEntity queueEvaluationCandidate(EvaluationCandidateData data, String actor);
}
