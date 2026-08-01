/*
 * Purpose: Verifies the AI-4 supervised fine-tuning lifecycle.
 * Why it exists: The lifecycle must validate datasets, select a model, produce adapters/reports/model cards, and support rollback without deployment.
 * Architecture fit: Unit coverage for the fine-tuning application service.
 */
package com.airural.platform.core.finetuning;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.datasets.domain.DatasetEntity;
import com.airural.platform.core.datasets.infrastructure.DatasetRepository;
import com.airural.platform.core.finetuning.application.*;
import com.airural.platform.core.finetuning.domain.*;
import com.airural.platform.core.finetuning.infrastructure.*;
import com.airural.platform.core.finetuning.web.dto.FineTuningDtos.*;
import com.airural.platform.core.knowledge.infrastructure.KnowledgeDatasetRepository;
import com.airural.platform.core.training.application.DatasetResolver;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for fine-tuning lifecycle service. */
class FineTuningLifecycleServiceTests {
    private FineTuningRunRepository runs;
    private AdapterVersionRepository adapters;
    private FineTuningTrainingMetricsRepository trainingMetrics;
    private EvaluationMetricsRepository evaluationMetrics;
    private ModelCardRepository modelCards;
    private TrainingReportRepository reports;
    private TrainingApprovalRepository approvals;
    private DatasetRepository datasetRepository;
    private FineTuningLifecycleService service;

    @BeforeEach
    void setUp() {
        runs = mock(FineTuningRunRepository.class);
        adapters = mock(AdapterVersionRepository.class);
        trainingMetrics = mock(FineTuningTrainingMetricsRepository.class);
        evaluationMetrics = mock(EvaluationMetricsRepository.class);
        modelCards = mock(ModelCardRepository.class);
        reports = mock(TrainingReportRepository.class);
        approvals = mock(TrainingApprovalRepository.class);
        datasetRepository = mock(DatasetRepository.class);
        when(runs.save(any(FineTuningRunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new FineTuningLifecycleService(
                runs,
                adapters,
                trainingMetrics,
                evaluationMetrics,
                modelCards,
                reports,
                approvals,
                new DatasetResolver(datasetRepository, mock(KnowledgeDatasetRepository.class)),
                new BaseModelBenchmarkService());
    }

    @Test
    void startsFineTuningLifecycleAndProducesGovernedArtifacts() {
        UUID datasetId = UUID.randomUUID();
        when(datasetRepository.findById(datasetId)).thenReturn(Optional.of(new DatasetEntity(datasetId, "approved", "QA", "VALIDATED", UUID.randomUUID(), "desc", "[]", "{}", BigDecimal.ONE, BigDecimal.ZERO, Instant.now(), Instant.now())));

        FineTuningRunResponse response = service.start(new StartFineTuningRequest(
                "rural intelligence sft",
                "AI1_DATASET",
                datasetId,
                List.of("Phi", "Qwen", "Llama"),
                List.of("Survey Understanding", "Root Cause Reasoning", "Citation Formatting"),
                Map.of("learningRate", 0.0002),
                true));

        assertThat(response.status()).isEqualTo("COMPLETED_REVIEW_APPROVED");
        assertThat(response.selectedModelFamily()).isEqualTo("QWEN");
        assertThat(response.reviewStatus()).isEqualTo("ALL_REVIEW_BOARDS_APPROVED");
        verify(adapters, times(2)).save(any());
        verify(trainingMetrics).save(any());
        verify(evaluationMetrics).save(any());
        verify(modelCards).save(any());
        verify(reports, times(4)).save(any());
        verify(approvals, times(7)).save(any());
    }

    @Test
    void rejectsUnsupportedFineTuningTasks() {
        UUID datasetId = UUID.randomUUID();
        when(datasetRepository.findById(datasetId)).thenReturn(Optional.of(new DatasetEntity(datasetId, "approved", "QA", "VALIDATED", UUID.randomUUID(), "desc", "[]", "{}", BigDecimal.ONE, BigDecimal.ZERO, Instant.now(), Instant.now())));

        assertThatThrownBy(() -> service.start(new StartFineTuningRequest("bad", "AI1_DATASET", datasetId, List.of("Qwen"), List.of("unsupported task"), Map.of(), false)))
                .isInstanceOf(FineTuningException.class)
                .hasMessageContaining("unsupported task");
    }

    @Test
    void rollbackMarksRunWithoutProductionDeploymentChange() {
        UUID runId = UUID.randomUUID();
        FineTuningRunEntity run = new FineTuningRunEntity(runId, "run", "Qwen", "QWEN", "QLORA", "COMPLETED_REVIEW_APPROVED", "AI1_DATASET", UUID.randomUUID(), "{}", "{}", "APPROVED", Instant.now(), Instant.now(), Instant.now());
        when(runs.findById(runId)).thenReturn(Optional.of(run));
        when(runs.save(any(FineTuningRunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FineTuningOperationResponse response = service.rollback(new RollbackRequest(runId, "external audit rollback"));

        assertThat(response.status()).isEqualTo("ROLLED_BACK");
        assertThat(response.details()).contains("no production model");
        verify(reports).save(any());
    }
}
