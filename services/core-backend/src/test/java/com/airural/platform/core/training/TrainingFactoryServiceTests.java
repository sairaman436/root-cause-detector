/*
 * Purpose: Verifies enterprise model training factory orchestration behavior.
 * Why it exists: AI-3 must create governed infrastructure records without running LoRA, QLoRA, or production training.
 * Architecture fit: Unit coverage for training job manager, scheduler, model registry, adapter registry, and checkpoint restore workflows.
 */
package com.airural.platform.core.training;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.airural.platform.core.training.application.*;
import com.airural.platform.core.training.domain.*;
import com.airural.platform.core.training.infrastructure.*;
import com.airural.platform.core.training.web.dto.TrainingDtos.*;
import com.airural.platform.core.datasets.domain.DatasetEntity;
import com.airural.platform.core.datasets.infrastructure.DatasetRepository;
import com.airural.platform.core.knowledge.infrastructure.KnowledgeDatasetRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for training factory service. */
class TrainingFactoryServiceTests {
    private TrainingJobRepository jobs;
    private TrainingRunRepository runs;
    private TrainingExperimentRepository experiments;
    private HyperparameterSetRepository hyperparameters;
    private TrainingQueueRepository queue;
    private TrainingLogRepository logs;
    private TrainingArtifactRepository artifacts;
    private ModelRegistryRepository models;
    private AdapterRegistryRepository adapters;
    private TrainingCheckpointRepository checkpoints;
    private DatasetRepository datasetRepository;
    private GPUResourceRepository gpuResources;
    private TrainingFactoryService service;

    @BeforeEach
    void setUp() {
        jobs = mock(TrainingJobRepository.class);
        runs = mock(TrainingRunRepository.class);
        experiments = mock(TrainingExperimentRepository.class);
        hyperparameters = mock(HyperparameterSetRepository.class);
        queue = mock(TrainingQueueRepository.class);
        logs = mock(TrainingLogRepository.class);
        artifacts = mock(TrainingArtifactRepository.class);
        models = mock(ModelRegistryRepository.class);
        adapters = mock(AdapterRegistryRepository.class);
        checkpoints = mock(TrainingCheckpointRepository.class);
        datasetRepository = mock(DatasetRepository.class);
        gpuResources = mock(GPUResourceRepository.class);
        when(jobs.save(any(TrainingJobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(runs.save(any(TrainingRunEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(experiments.save(any(TrainingExperimentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hyperparameters.save(any(HyperparameterSetEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new TrainingFactoryService(
                jobs,
                runs,
                experiments,
                hyperparameters,
                queue,
                logs,
                mock(TrainingMetricsRepository.class),
                artifacts,
                models,
                adapters,
                checkpoints,
                new DatasetResolver(datasetRepository, mock(KnowledgeDatasetRepository.class)),
                new GPUResourceManager(gpuResources),
                new CheckpointManager(checkpoints));
    }

    @Test
    void createsQueuedLoraJobWithRegistryArtifactsAndApprovedLineage() {
        UUID datasetId = UUID.randomUUID();
        when(datasetRepository.findById(datasetId)).thenReturn(Optional.of(new DatasetEntity(datasetId, "approved", "QA", "VALIDATED", UUID.randomUUID(), "desc", "[]", "{}", BigDecimal.ONE, BigDecimal.ZERO, Instant.now(), Instant.now())));

        TrainingJobResponse response = service.createJob(new CreateTrainingJobRequest(
                "Qwen rural QA",
                "qwen2.5-7b",
                "Qwen",
                "LoRA",
                "AI1_DATASET",
                datasetId,
                10,
                1,
                24,
                true,
                true,
                true,
                Map.of("learningRate", 0.0002),
                "rural qa experiment",
                "dataset dry run"));

        assertThat(response.status()).isEqualTo("QUEUED");
        assertThat(response.trainingMethod()).isEqualTo("LORA");
        verify(queue).save(any());
        verify(models).save(any());
        verify(adapters).save(any());
        verify(artifacts).save(any());
        verify(logs).save(any());
    }

    @Test
    void schedulesJobWhenGpuCapacityExistsWithoutLaunchingTraining() {
        UUID jobId = UUID.randomUUID();
        TrainingJobEntity job = trainingJob(jobId);
        when(jobs.findById(jobId)).thenReturn(Optional.of(job));
        when(jobs.save(any(TrainingJobEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gpuResources.findByStatus("AVAILABLE")).thenReturn(List.of(new GPUResourceEntity(UUID.randomUUID(), "local", "SINGLE_GPU_READY", 1, 24, 0, "AVAILABLE", Instant.now())));

        TrainingRunResponse response = service.start(new StartTrainingRequest(jobId));

        assertThat(response.status()).isEqualTo("SCHEDULED");
        assertThat(response.schedulerDecision()).isEqualTo("CAPACITY_RESERVED");
        verify(runs).save(any());
    }

    @Test
    void rejectsUnsupportedTrainingMethods() {
        UUID datasetId = UUID.randomUUID();

        assertThatThrownBy(() -> service.createJob(new CreateTrainingJobRequest(
                "bad",
                "qwen",
                "Qwen",
                "RLHF",
                "AI1_DATASET",
                datasetId,
                1,
                1,
                24,
                true,
                false,
                true,
                Map.of(),
                null,
                null)))
                .isInstanceOf(TrainingException.class)
                .hasMessageContaining("LoRA");
    }

    @Test
    void recordsCheckpointRestoreRequestOnlyForValidRestorableCheckpoint() {
        UUID checkpointId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(checkpoints.findById(checkpointId)).thenReturn(Optional.of(new TrainingCheckpointEntity(checkpointId, jobId, null, 100, "AUTOMATIC", "s3://checkpoint", "checksum", "VALID", true, Instant.now())));

        TrainingOperationResponse response = service.restore(new RestoreCheckpointRequest(checkpointId, "resume dry run"));

        assertThat(response.status()).isEqualTo("RESTORE_REQUESTED");
        verify(logs).save(any());
    }

    private TrainingJobEntity trainingJob(UUID jobId) {
        return new TrainingJobEntity(jobId, UUID.randomUUID(), UUID.randomUUID(), "job", "qwen", "QWEN", "QLORA", "AI1_DATASET", UUID.randomUUID(), "QUEUED", 1, 1, 24, true, true, true, "{}", Instant.now(), Instant.now());
    }
}
