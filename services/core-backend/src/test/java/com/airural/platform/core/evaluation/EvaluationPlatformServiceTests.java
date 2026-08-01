/*
 * Purpose: Verifies the AI-5 independent evaluation platform workflows.
 * Why it exists: The platform must evaluate fine-tuned models, record immutable benchmark/safety/comparison evidence, and support promote/reject decisions without deployment.
 * Architecture fit: Unit coverage for the evaluation application service.
 */
package com.airural.platform.core.evaluation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.evaluation.application.*;
import com.airural.platform.core.evaluation.domain.*;
import com.airural.platform.core.evaluation.infrastructure.*;
import com.airural.platform.core.evaluation.web.dto.EvaluationDtos.*;
import com.airural.platform.core.finetuning.domain.FineTuningRunEntity;
import com.airural.platform.core.finetuning.infrastructure.FineTuningRunRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for AI evaluation service. */
class EvaluationPlatformServiceTests {
    private FineTuningRunRepository fineTuningRuns;
    private BenchmarkSuiteRepository suites;
    private EvaluationRunRepository evaluations;
    private BenchmarkRunRepository benchmarkRuns;
    private EvaluationMetricRepository metrics;
    private SafetyTestRepository safetyTests;
    private RedTeamRunRepository redTeamRuns;
    private HallucinationReportRepository hallucinationReports;
    private CitationReportRepository citationReports;
    private ModelComparisonRepository comparisons;
    private EvaluationApprovalRepository approvals;
    private EvaluationPlatformService service;

    @BeforeEach
    void setUp() {
        fineTuningRuns = mock(FineTuningRunRepository.class);
        suites = mock(BenchmarkSuiteRepository.class);
        evaluations = mock(EvaluationRunRepository.class);
        benchmarkRuns = mock(BenchmarkRunRepository.class);
        metrics = mock(EvaluationMetricRepository.class);
        safetyTests = mock(SafetyTestRepository.class);
        redTeamRuns = mock(RedTeamRunRepository.class);
        hallucinationReports = mock(HallucinationReportRepository.class);
        citationReports = mock(CitationReportRepository.class);
        comparisons = mock(ModelComparisonRepository.class);
        approvals = mock(EvaluationApprovalRepository.class);
        when(evaluations.save(any(EvaluationRunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(suites.save(any(BenchmarkSuiteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new EvaluationPlatformService(fineTuningRuns, suites, evaluations, benchmarkRuns, metrics, safetyTests, redTeamRuns, hallucinationReports, citationReports, comparisons, approvals);
    }

    @Test
    void runsImmutableEvaluationAndRecordsAllEvidence() {
        UUID modelRunId = UUID.randomUUID();
        when(fineTuningRuns.findById(modelRunId)).thenReturn(Optional.of(modelRun(modelRunId, "COMPLETED_REVIEW_APPROVED")));
        when(suites.findBySuiteKey(any())).thenReturn(Optional.empty());

        EvaluationRunResponse response = service.run(new RunEvaluationRequest(modelRunId, "PRODUCTION_READINESS", List.of("survey-understanding", "citation-accuracy"), true, true));

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.recommendation()).isEqualTo("PROMOTE");
        assertThat(response.overallScore()).isGreaterThan(BigDecimal.valueOf(0.90));
        verify(benchmarkRuns, times(2)).save(any());
        verify(metrics).save(any());
        verify(safetyTests, times(9)).save(any());
        verify(redTeamRuns, times(8)).save(any());
        verify(hallucinationReports).save(any());
        verify(citationReports).save(any());
        verify(comparisons).save(any());
        verify(approvals, times(6)).save(any());
    }

    @Test
    void blocksEvaluationForModelsNotReady() {
        UUID modelRunId = UUID.randomUUID();
        when(fineTuningRuns.findById(modelRunId)).thenReturn(Optional.of(modelRun(modelRunId, "IN_PROGRESS")));

        assertThatThrownBy(() -> service.run(new RunEvaluationRequest(modelRunId, "PRODUCTION_READINESS", List.of(), true, false)))
                .isInstanceOf(EvaluationException.class)
                .hasMessageContaining("completed fine-tuned adapters");
    }

    @Test
    void recordsPromotionRecommendationWithoutDeployment() {
        UUID evaluationId = UUID.randomUUID();
        when(evaluations.findById(evaluationId)).thenReturn(Optional.of(evaluation(evaluationId, "PROMOTE")));

        EvaluationDecisionResponse response = service.promote(new PromotionRequest(evaluationId, "meets gate"));

        assertThat(response.status()).isEqualTo("PROMOTION_RECOMMENDED");
        assertThat(response.details()).contains("no deployment");
        verify(approvals).save(any());
    }

    @Test
    void rejectsPromotionWhenRecommendationIsReject() {
        UUID evaluationId = UUID.randomUUID();
        when(evaluations.findById(evaluationId)).thenReturn(Optional.of(evaluation(evaluationId, "REJECT")));

        assertThatThrownBy(() -> service.promote(new PromotionRequest(evaluationId, "override")))
                .isInstanceOf(EvaluationException.class)
                .hasMessageContaining("does not permit promotion");
    }

    private FineTuningRunEntity modelRun(UUID id, String status) {
        return new FineTuningRunEntity(id, "rural adapter", "Qwen", "QWEN", "QLORA", status, "AI1_DATASET", UUID.randomUUID(), "{}", "{}", "APPROVED", Instant.now(), Instant.now(), Instant.now());
    }

    private EvaluationRunEntity evaluation(UUID id, String recommendation) {
        return new EvaluationRunEntity(id, UUID.randomUUID(), "rural adapter", "QWEN", "PRODUCTION_READINESS", "COMPLETED", recommendation, BigDecimal.valueOf(0.91), "hash", "{}", Instant.now(), Instant.now());
    }
}
