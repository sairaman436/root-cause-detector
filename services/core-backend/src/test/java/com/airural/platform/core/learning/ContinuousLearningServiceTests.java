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
import com.airural.platform.core.identity.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Test
    void authenticatedCorrectionPreservesOriginalAndCorrectedOutput() {
        UUID recordId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        LearningRecordEntity learningRecord = new LearningRecordEntity(recordId, Instant.now(), "REAL_GOVERNED_INTERACTION", "qwen", "prompt-1", "retrieved policy context", "What should be verified?", "Original answer", null, null, BigDecimal.valueOf(0.7), "[{\"source_id\":\"policy-1\",\"valid\":true,\"supports_claim\":true}]", "", "", false, "INTERNAL", "PENDING_HUMAN_REVIEW", "root-cause-analysis", "village-water-1", false, null);
        TrainingCandidateEntity trainingCandidate = new TrainingCandidateEntity(candidateId, recordId, "future", "REAL_GOVERNED_INTERACTION", BigDecimal.valueOf(0.8), "", "learning-record:" + recordId, "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now(), null, null, false);
        when(records.findById(recordId)).thenReturn(Optional.of(learningRecord));
        when(candidates.findById(candidateId)).thenReturn(Optional.of(trainingCandidate));
        AuthenticatedUser reviewer = reviewer(reviewerId);

        LearningDecisionResponse response = service.reviewCandidate(candidateId, new CandidateReviewRequest("CORRECT", "Corrected answer", "Verified against policy evidence"), reviewer);

        assertThat(response.status()).isEqualTo("APPROVED_FOR_DATASET");
        assertThat(learningRecord.getAcceptedOutput()).isEqualTo("Corrected answer");
        assertThat(learningRecord.getAiOutput()).isEqualTo("Original answer");
        assertThat(trainingCandidate.getReviewDecision()).isEqualTo("CORRECT");
        assertThat(trainingCandidate.getReviewerUserId()).isEqualTo(reviewerId);
        verify(corrections).save(argThat(correction -> correction.getOriginalText().equals("Original answer") && correction.getCorrectedText().equals("Corrected answer")));
        verify(reviews).save(argThat(review -> review.getReviewerUserId().equals(reviewerId) && review.getDecision().equals("CORRECT")));
    }

    @Test
    void syntheticCandidateCannotBeApproved() {
        UUID recordId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        LearningRecordEntity learningRecord = new LearningRecordEntity(recordId, Instant.now(), "SYNTHETIC_DEVELOPMENT_FIXTURE", "qwen", "prompt-1", "context", "input", "output", null, null, BigDecimal.valueOf(0.7), "[{\"source_id\":\"synthetic\",\"valid\":true,\"supports_claim\":true}]", "", "", false, "PUBLIC_SYNTHETIC", "PENDING_HUMAN_REVIEW", "root-cause-analysis", "synthetic-1", true, null);
        TrainingCandidateEntity trainingCandidate = new TrainingCandidateEntity(candidateId, recordId, "future", "SYNTHETIC_DEVELOPMENT_FIXTURE", BigDecimal.valueOf(0.8), "", "synthetic", "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now(), null, null, true);
        when(records.findById(recordId)).thenReturn(Optional.of(learningRecord));
        when(candidates.findById(candidateId)).thenReturn(Optional.of(trainingCandidate));

        assertThatThrownBy(() -> service.reviewCandidate(candidateId, new CandidateReviewRequest("APPROVE", null, "not production"), reviewer(UUID.randomUUID())))
                .isInstanceOf(LearningException.class)
                .hasMessageContaining("Synthetic development records cannot enter the production dataset");
        verify(reviews, never()).save(any());
    }

    @Test
    void rejectionRequiresReason() {
        UUID recordId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        when(records.findById(recordId)).thenReturn(Optional.of(record(recordId)));
        when(candidates.findById(candidateId)).thenReturn(Optional.of(new TrainingCandidateEntity(candidateId, recordId, "future", "REAL_GOVERNED_INTERACTION", BigDecimal.valueOf(0.8), "", "lineage", "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now(), null, null, false)));

        assertThatThrownBy(() -> service.reviewCandidate(candidateId, new CandidateReviewRequest("REJECT", null, ""), reviewer(UUID.randomUUID())))
                .isInstanceOf(LearningException.class)
                .hasMessageContaining("A rejection reason is required");
    }

    @Test
    void datasetExportIncludesOnlyApprovedRealCandidate() {
        UUID recordId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        LearningRecordEntity learningRecord = new LearningRecordEntity(recordId, Instant.now(), "REAL_GOVERNED_INTERACTION", "qwen", "prompt-1", "context", "input", "approved output", null, "approved output", BigDecimal.valueOf(0.7), "[{\"source_id\":\"policy-1\",\"valid\":true,\"supports_claim\":true}]", "", "", true, "INTERNAL", "APPROVED_FOR_DATASET", "root-cause-analysis", "village-water-1", false, "dataset-v0.1");
        TrainingCandidateEntity trainingCandidate = new TrainingCandidateEntity(candidateId, recordId, "dataset-v0.1", "REAL_GOVERNED_INTERACTION", BigDecimal.valueOf(0.8), "reviewer", "lineage", "READY_FOR_DATASET_VALIDATION", "APPROVED_FOR_DATASET", Instant.now(), UUID.randomUUID(), "dataset-v0.1", false);
        trainingCandidate.review("APPROVE", "APPROVED_FOR_DATASET", "READY_FOR_DATASET_VALIDATION", UUID.randomUUID(), "dataset-v0.1");
        when(candidates.findByApprovalStatusOrderByCreatedAtAsc("APPROVED_FOR_DATASET")).thenReturn(List.of(trainingCandidate));
        when(records.findById(recordId)).thenReturn(Optional.of(learningRecord));

        DatasetExportResponse response = service.datasetExport();

        assertThat(response.status()).isEqualTo("READY_FOR_JSONL_VALIDATION");
        assertThat(response.examples()).hasSize(1);
        assertThat(response.examples().getFirst().datasetVersion()).isEqualTo("dataset-v0.1");
    }

    private AuthenticatedUser reviewer(UUID id) {
        return new AuthenticatedUser(id, "reviewer@example.org", List.of(new SimpleGrantedAuthority("LEARNING_REVIEW")));
    }

    private LearningRecordEntity record(UUID id) {
        return new LearningRecordEntity(id, Instant.now(), "Survey Officer", "rural-v1", "prompt-v1", "context", "input", "output", null, null, BigDecimal.valueOf(0.55), "{}", null, "reviewer", false, "INTERNAL", "PENDING_HUMAN_REVIEW");
    }

    private TrainingCandidateEntity candidate(UUID id) {
        return new TrainingCandidateEntity(id, UUID.randomUUID(), "future", "Survey Officer", BigDecimal.valueOf(0.82), "reviewer", "lineage", "PENDING_DATASET_APPROVAL", "PENDING_APPROVAL", Instant.now());
    }
}
