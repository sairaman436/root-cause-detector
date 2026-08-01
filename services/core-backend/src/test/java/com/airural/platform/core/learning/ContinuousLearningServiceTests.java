/*
 * Purpose: Verifies the AI-7 continuous learning workflow.
 * Why it exists: Feedback must become governed learning candidates only through review, approval, metrics, and audit records.
 * Architecture fit: Unit coverage for the continuous learning application service.
 */
package com.airural.platform.core.learning;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.learning.application.*;
import com.airural.platform.core.learning.domain.*;
import com.airural.platform.core.learning.infrastructure.*;
import com.airural.platform.core.learning.web.dto.LearningDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for continuous learning service. */
class ContinuousLearningServiceTests {
    private LearningRecordRepository records;
    private FeedbackEventRepository feedbackEvents;
    private CorrectionRepository corrections;
    private HumanReviewRepository reviews;
    private KnowledgeDeltaRepository knowledgeDeltas;
    private TrainingCandidateRepository candidates;
    private ApprovalWorkflowRepository approvals;
    private LearningMetricsRepository metrics;
    private LearningAuditRepository audits;
    private ContinuousLearningService service;

    @BeforeEach
    void setUp() {
        records = mock(LearningRecordRepository.class);
        feedbackEvents = mock(FeedbackEventRepository.class);
        corrections = mock(CorrectionRepository.class);
        reviews = mock(HumanReviewRepository.class);
        knowledgeDeltas = mock(KnowledgeDeltaRepository.class);
        candidates = mock(TrainingCandidateRepository.class);
        approvals = mock(ApprovalWorkflowRepository.class);
        metrics = mock(LearningMetricsRepository.class);
        audits = mock(LearningAuditRepository.class);
        when(records.save(any(LearningRecordEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(candidates.save(any(TrainingCandidateEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metrics.save(any(LearningMetricsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new ContinuousLearningService(records, feedbackEvents, corrections, reviews, knowledgeDeltas, candidates, approvals, metrics, audits);
    }

    @Test
    void capturesFeedbackCorrectionKnowledgeDeltaAndAudit() {
        LearningRecordResponse response = service.feedback(new FeedbackRequest("Policy Expert", "rural-v1", "prompt-v2", "context", "input user@example.com", "wrong output", "correct output", "accepted", BigDecimal.valueOf(0.42), "{\"evidence\":true}", "Knowledge Agent", "reviewer", "Policy Expert", "KNOWLEDGE_UPDATE", "new policy", "RESTRICTED"));

        assertThat(response.approvalStatus()).isEqualTo("PENDING_HUMAN_REVIEW");
        assertThat(response.privacyClassification()).isEqualTo("RESTRICTED");
        verify(feedbackEvents).save(any());
        verify(corrections).save(any());
        verify(knowledgeDeltas).save(any());
        verify(audits).save(any());
    }

    @Test
    void reviewCreatesTrainingCandidateWhenRequested() {
        UUID recordId = UUID.randomUUID();
        when(records.findById(recordId)).thenReturn(Optional.of(record(recordId)));

        LearningDecisionResponse response = service.review(new ReviewRequest(recordId, "expert", "ACCEPT", "good", true));

        assertThat(response.decision()).isEqualTo("CANDIDATE_CREATED");
        assertThat(response.details()).contains("no model retraining");
        verify(reviews).save(any());
        verify(candidates).save(any());
        verify(approvals).save(any());
        verify(audits, times(2)).save(any());
    }

    @Test
    void promoteApprovesFutureDatasetOnly() {
        UUID candidateId = UUID.randomUUID();
        when(candidates.findById(candidateId)).thenReturn(Optional.of(candidate(candidateId)));

        LearningDecisionResponse response = service.promote(new PromoteLearningRequest(candidateId, "governance", "approved"));

        assertThat(response.status()).isEqualTo("APPROVED_FOR_FUTURE_DATASET");
        assertThat(response.details()).contains("no production retraining");
        verify(approvals).save(any());
        verify(audits).save(any());
    }

    @Test
    void rejectsMissingCandidate() {
        UUID candidateId = UUID.randomUUID();
        when(candidates.findById(candidateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(new RejectLearningRequest(candidateId, "governance", "bad quality")))
                .isInstanceOf(LearningException.class)
                .hasMessageContaining("Training candidate was not found");
    }

    private LearningRecordEntity record(UUID id) {
        return new LearningRecordEntity(id, Instant.now(), "Survey Officer", "rural-v1", "prompt-v1", "context", "input", "output", null, null, BigDecimal.valueOf(0.55), "{}", null, "reviewer", false, "INTERNAL", "PENDING_HUMAN_REVIEW");
    }

    private TrainingCandidateEntity candidate(UUID id) {
        return new TrainingCandidateEntity(id, UUID.randomUUID(), "future", "Survey Officer", BigDecimal.valueOf(0.82), "reviewer", "lineage", "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now());
    }
}
