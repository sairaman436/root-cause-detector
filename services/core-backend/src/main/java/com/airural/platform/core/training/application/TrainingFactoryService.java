/*
 * Purpose: Coordinates training job creation, queueing, scheduling, cancellation, registry metadata, and checkpoint restore requests.
 * Why it exists: Future Rural Intelligence Foundation Models need governed training infrastructure before any LoRA, QLoRA, or full fine-tuning is run.
 * Architecture fit: AI-3 application service; it records and schedules metadata only and never launches production training.
 */
package com.airural.platform.core.training.application;

import com.airural.platform.core.training.domain.*;
import com.airural.platform.core.training.infrastructure.*;
import com.airural.platform.core.training.web.dto.TrainingDtos.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for the enterprise model training factory. */
@Service
public class TrainingFactoryService {
    private static final Set<String> SUPPORTED_METHODS = Set.of("LORA", "QLORA", "FULL_FINE_TUNING");
    private static final Set<String> SUPPORTED_FAMILIES = Set.of("QWEN", "LLAMA", "GEMMA", "MISTRAL", "DEEPSEEK", "PHI", "FUTURE_OPEN_WEIGHT");

    private final TrainingJobRepository jobs;
    private final TrainingRunRepository runs;
    private final TrainingExperimentRepository experiments;
    private final HyperparameterSetRepository hyperparameters;
    private final TrainingQueueRepository queue;
    private final TrainingLogRepository logs;
    private final TrainingMetricsRepository metrics;
    private final TrainingArtifactRepository artifacts;
    private final ModelRegistryRepository models;
    private final AdapterRegistryRepository adapters;
    private final TrainingCheckpointRepository checkpoints;
    private final DatasetResolver datasetResolver;
    private final GPUResourceManager gpuResourceManager;
    private final CheckpointManager checkpointManager;

    public TrainingFactoryService(
            TrainingJobRepository jobs,
            TrainingRunRepository runs,
            TrainingExperimentRepository experiments,
            HyperparameterSetRepository hyperparameters,
            TrainingQueueRepository queue,
            TrainingLogRepository logs,
            TrainingMetricsRepository metrics,
            TrainingArtifactRepository artifacts,
            ModelRegistryRepository models,
            AdapterRegistryRepository adapters,
            TrainingCheckpointRepository checkpoints,
            DatasetResolver datasetResolver,
            GPUResourceManager gpuResourceManager,
            CheckpointManager checkpointManager) {
        this.jobs = jobs;
        this.runs = runs;
        this.experiments = experiments;
        this.hyperparameters = hyperparameters;
        this.queue = queue;
        this.logs = logs;
        this.metrics = metrics;
        this.artifacts = artifacts;
        this.models = models;
        this.adapters = adapters;
        this.checkpoints = checkpoints;
        this.datasetResolver = datasetResolver;
        this.gpuResourceManager = gpuResourceManager;
        this.checkpointManager = checkpointManager;
    }

    /** Creates a validated, queued training job without launching training. */
    @Transactional
    public TrainingJobResponse createJob(CreateTrainingJobRequest request) {
        validateTrainingMethod(request.trainingMethod());
        validateModelFamily(request.modelFamily());
        String lineage = datasetResolver.resolveApproved(request.datasetSourceType(), request.datasetId());
        Instant now = Instant.now();
        TrainingExperimentEntity experiment = experiments.save(new TrainingExperimentEntity(
                UUID.randomUUID(),
                request.experimentName() == null ? request.jobName() + " experiment" : request.experimentName(),
                request.experimentDescription(),
                "model-engineering",
                "ACTIVE",
                "{\"reviewBoards\":[\"Architecture\",\"ML\",\"Security\",\"Performance\",\"MLOps\"]}",
                now,
                now));
        HyperparameterSetEntity hp = hyperparameters.save(new HyperparameterSetEntity(
                UUID.randomUUID(),
                request.jobName() + " hyperparameters",
                serializeMap(request.hyperparameters()),
                Boolean.TRUE.equals(request.mixedPrecisionReady()) ? "MIXED_PRECISION_READY" : "STANDARD_PRECISION",
                Boolean.TRUE.equals(request.resumeEnabled()),
                now));
        TrainingJobEntity job = jobs.save(new TrainingJobEntity(
                UUID.randomUUID(),
                experiment.getId(),
                hp.getId(),
                request.jobName(),
                request.baseModel(),
                request.modelFamily().toUpperCase(),
                request.trainingMethod().toUpperCase(),
                request.datasetSourceType(),
                request.datasetId(),
                "VALIDATED",
                request.priority() == null ? 50 : request.priority(),
                request.requestedGpuCount() == null ? 1 : request.requestedGpuCount(),
                request.requestedVramGb() == null ? 24 : request.requestedVramGb(),
                Boolean.TRUE.equals(request.mixedPrecisionReady()),
                Boolean.TRUE.equals(request.distributedReady()),
                Boolean.TRUE.equals(request.resumeEnabled()),
                lineage,
                now,
                now));
        job.markQueued();
        jobs.save(job);
        queue.save(new TrainingQueueEntity(UUID.randomUUID(), job.getId(), job.getPriority(), "READY", 0, now, now));
        models.save(new ModelRegistryEntity(UUID.randomUUID(), job.getId(), request.jobName() + " model", request.modelFamily().toUpperCase(), request.baseModel(), request.baseModel(), "ADAPTER_READY_METADATA", "SOURCE_LICENSE_REQUIRED", "{}", "{}", "{}", "REGISTERED", now));
        if ("LORA".equalsIgnoreCase(request.trainingMethod()) || "QLORA".equalsIgnoreCase(request.trainingMethod())) {
            adapters.save(new AdapterRegistryEntity(UUID.randomUUID(), job.getId(), request.jobName() + " adapter", request.trainingMethod().toUpperCase(), request.baseModel(), "s3://airural-training/" + job.getId() + "/adapters/pending", checksum(job.getId().toString()), "PLANNED", now));
        }
        artifacts.save(new TrainingArtifactEntity(UUID.randomUUID(), job.getId(), null, "TRAINING_CONFIGURATION", "s3://airural-training/" + job.getId() + "/config/training.json", checksum(job.getId() + ":config"), 0L, "PENDING_WRITE", now));
        logs.save(new TrainingLogEntity(UUID.randomUUID(), job.getId(), "INFO", "JOB_VALIDATED", "Training job validated and queued; no training execution was launched.", lineage, now));
        return toJobResponse(job);
    }

    /** Lists training jobs for dashboard APIs. */
    @Transactional(readOnly = true)
    public Page<TrainingJobResponse> jobs(Pageable pageable) {
        return jobs.findAll(pageable).map(this::toJobResponse);
    }

    /** Schedules a validated job if GPU capacity is available, otherwise leaves it queued. */
    @Transactional
    public TrainingRunResponse start(StartTrainingRequest request) {
        TrainingJobEntity job = jobs.findById(request.jobId())
                .orElseThrow(() -> new TrainingException(HttpStatus.NOT_FOUND, "TRAINING_JOB_NOT_FOUND", "Training job was not found"));
        var resource = gpuResourceManager.allocate(job.getRequestedGpuCount(), job.getRequestedVramGb());
        String status = resource.isPresent() ? "SCHEDULED" : "QUEUED_WAITING_FOR_GPU";
        if (resource.isPresent()) {
            job.markScheduled();
            jobs.save(job);
        }
        TrainingRunEntity run = runs.save(new TrainingRunEntity(
                UUID.randomUUID(),
                job.getId(),
                resource.map(GPUResourceEntity::getId).orElse(null),
                status,
                Instant.now(),
                null,
                null,
                resource.isPresent() ? "GPU capacity reserved; worker execution remains disabled in AI-3." : "No GPU capacity available; job remains queued."));
        logs.save(new TrainingLogEntity(UUID.randomUUID(), job.getId(), "INFO", "SCHEDULER_DECISION", run.getStatus(), "{}", Instant.now()));
        return new TrainingRunResponse(run.getId(), run.getJobId(), run.getStatus(), resource.isPresent() ? "CAPACITY_RESERVED" : "WAITING_FOR_GPU");
    }

    /** Cancels a queued or scheduled job. */
    @Transactional
    public TrainingOperationResponse cancel(CancelTrainingRequest request) {
        TrainingJobEntity job = jobs.findById(request.jobId())
                .orElseThrow(() -> new TrainingException(HttpStatus.NOT_FOUND, "TRAINING_JOB_NOT_FOUND", "Training job was not found"));
        job.cancel();
        jobs.save(job);
        logs.save(new TrainingLogEntity(UUID.randomUUID(), job.getId(), "WARN", "JOB_CANCELLED", request.reason() == null ? "Cancelled by authorized user" : request.reason(), "{}", Instant.now()));
        return new TrainingOperationResponse(job.getId(), "CANCEL", "CANCELLED", "Training job cancelled before execution", Instant.now());
    }

    /** Lists experiment registry records. */
    @Transactional(readOnly = true)
    public Page<ExperimentResponse> experiments(Pageable pageable) {
        return experiments.findAll(pageable).map(experiment -> new ExperimentResponse(experiment.getId(), experiment.getName(), experiment.getStatus()));
    }

    /** Lists model registry metadata. */
    @Transactional(readOnly = true)
    public Page<ModelResponse> models(Pageable pageable) {
        return models.findAll(pageable).map(model -> new ModelResponse(model.getId(), model.getModelName(), model.getModelFamily(), model.getStatus()));
    }

    /** Lists checkpoint metadata. */
    @Transactional(readOnly = true)
    public Page<CheckpointResponse> checkpoints(Pageable pageable) {
        return checkpoints.findAll(pageable).map(checkpoint -> new CheckpointResponse(checkpoint.getId(), checkpoint.getJobId(), checkpoint.getValidationStatus(), checkpoint.getRestorable()));
    }

    /** Validates checkpoint restore request and records audit evidence. */
    @Transactional
    public TrainingOperationResponse restore(RestoreCheckpointRequest request) {
        TrainingCheckpointEntity checkpoint = checkpointManager.validateRestore(request.checkpointId());
        logs.save(new TrainingLogEntity(UUID.randomUUID(), checkpoint.getJobId(), "INFO", "CHECKPOINT_RESTORE_REQUESTED", request.reason() == null ? "Restore requested" : request.reason(), "{\"checkpointId\":\"" + checkpoint.getId() + "\"}", Instant.now()));
        return new TrainingOperationResponse(checkpoint.getId(), "CHECKPOINT_RESTORE", "RESTORE_REQUESTED", "Restore request recorded; worker execution remains disabled in AI-3", Instant.now());
    }

    private void validateTrainingMethod(String method) {
        if (!SUPPORTED_METHODS.contains(method.toUpperCase())) {
            throw new TrainingException(HttpStatus.BAD_REQUEST, "TRAINING_METHOD_UNSUPPORTED", "Training method must be LoRA, QLoRA, or future full fine tuning");
        }
    }

    private void validateModelFamily(String modelFamily) {
        String normalized = modelFamily.toUpperCase();
        if (!SUPPORTED_FAMILIES.contains(normalized)) {
            throw new TrainingException(HttpStatus.BAD_REQUEST, "TRAINING_MODEL_UNSUPPORTED", "Model family is not supported by the training factory");
        }
    }

    private TrainingJobResponse toJobResponse(TrainingJobEntity job) {
        return new TrainingJobResponse(job.getId(), job.getJobName(), job.getBaseModel(), job.getModelFamily(), job.getTrainingMethod(), job.getStatus(), job.getPriority(), job.getRequestedGpuCount());
    }

    private String serializeMap(java.util.Map<String, Object> values) {
        return values == null ? "{}" : values.toString();
    }

    private String checksum(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new TrainingException(HttpStatus.INTERNAL_SERVER_ERROR, "TRAINING_CHECKSUM_FAILED", "Unable to calculate artifact checksum");
        }
    }
}
