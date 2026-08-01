/*
 * Purpose: Exposes enterprise model training factory APIs.
 * Why it exists: Model engineering teams need governed job, scheduler, experiment, model, and checkpoint endpoints before training workers exist.
 * Architecture fit: REST adapter for AI-3 with both platform-versioned and requested compatibility paths.
 */
package com.airural.platform.core.training.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.training.application.TrainingFactoryService;
import com.airural.platform.core.training.web.dto.TrainingDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for model training factory operations. */
@RestController
@RequestMapping({"/api/v1/training", "/training"})
public class TrainingController {
    private final TrainingFactoryService service;

    public TrainingController(TrainingFactoryService service) {
        this.service = service;
    }

    /** Creates a governed training job. */
    @Operation(summary = "Create training job", description = "Validates approved datasets, model family, method, hyperparameters, resource requirements, and queues metadata without executing training.")
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<TrainingJobResponse>> createJob(@Valid @RequestBody CreateTrainingJobRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createJob(body), RequestIds.from(request)));
    }

    /** Lists training jobs. */
    @Operation(summary = "List training jobs", description = "Lists training job manager records for dashboard APIs.")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Page<TrainingJobResponse>>> jobs(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.jobs(pageable), RequestIds.from(request)));
    }

    /** Starts scheduling without launching training execution. */
    @Operation(summary = "Start training scheduling", description = "Attempts GPU allocation and records a run; training workers remain disabled in AI-3.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TrainingRunResponse>> start(@Valid @RequestBody StartTrainingRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.start(body), RequestIds.from(request)));
    }

    /** Cancels a job. */
    @Operation(summary = "Cancel training job", description = "Cancels a queued or scheduled training job before execution.")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<TrainingOperationResponse>> cancel(@Valid @RequestBody CancelTrainingRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.cancel(body), RequestIds.from(request)));
    }

    /** Lists experiments. */
    @Operation(summary = "List training experiments", description = "Lists experiment registry metadata.")
    @GetMapping("/experiments")
    public ResponseEntity<ApiResponse<Page<ExperimentResponse>>> experiments(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.experiments(pageable), RequestIds.from(request)));
    }

    /** Lists models. */
    @Operation(summary = "List training model registry", description = "Lists model registry metadata including base model, adapter, and future serving metadata.")
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<Page<ModelResponse>>> models(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.models(pageable), RequestIds.from(request)));
    }

    /** Lists checkpoints. */
    @Operation(summary = "List training checkpoints", description = "Lists checkpoint metadata and validation status.")
    @GetMapping("/checkpoints")
    public ResponseEntity<ApiResponse<Page<CheckpointResponse>>> checkpoints(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.checkpoints(pageable), RequestIds.from(request)));
    }

    /** Records a checkpoint restore request. */
    @Operation(summary = "Restore training checkpoint", description = "Validates checkpoint restore readiness and records a restore request without launching workers.")
    @PostMapping("/checkpoints/restore")
    public ResponseEntity<ApiResponse<TrainingOperationResponse>> restore(@Valid @RequestBody RestoreCheckpointRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.restore(body), RequestIds.from(request)));
    }
}
