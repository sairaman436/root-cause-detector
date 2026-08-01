/*
 * Purpose: Exposes supervised fine-tuning lifecycle APIs.
 * Why it exists: AI-4 operators need controlled endpoints to start fine-tuning, list jobs/models/reports, and rollback adapter release candidates.
 * Architecture fit: REST adapter for the fine-tuning lifecycle with platform-versioned and requested compatibility paths.
 */
package com.airural.platform.core.finetuning.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.finetuning.application.FineTuningLifecycleService;
import com.airural.platform.core.finetuning.web.dto.FineTuningDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for AI-4 fine-tuning operations. */
@RestController
@RequestMapping({"/api/v1/finetuning", "/finetuning"})
public class FineTuningController {
    private final FineTuningLifecycleService service;

    public FineTuningController(FineTuningLifecycleService service) {
        this.service = service;
    }

    /** Starts the fine-tuning lifecycle. */
    @Operation(summary = "Start fine-tuning lifecycle", description = "Validates approved datasets, benchmarks base models, records adapter artifacts, metrics, reports, model card, and review approvals without deployment.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<FineTuningRunResponse>> start(@Valid @RequestBody StartFineTuningRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.start(body), RequestIds.from(request)));
    }

    /** Lists fine-tuning jobs. */
    @Operation(summary = "List fine-tuning jobs", description = "Lists supervised fine-tuning lifecycle runs.")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Page<FineTuningRunResponse>>> jobs(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.jobs(pageable), RequestIds.from(request)));
    }

    /** Lists fine-tuned model card records. */
    @Operation(summary = "List fine-tuned models", description = "Lists model cards for produced adapters.")
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<Page<FineTunedModelResponse>>> models(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.models(pageable), RequestIds.from(request)));
    }

    /** Lists fine-tuning report artifacts. */
    @Operation(summary = "List fine-tuning reports", description = "Lists benchmark, training, evaluation, loss curve, and rollback reports.")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<FineTuningReportResponse>>> reports(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reports(pageable), RequestIds.from(request)));
    }

    /** Rolls back an adapter release candidate. */
    @Operation(summary = "Rollback fine-tuning adapter", description = "Marks an adapter release candidate as rolled back without changing production models.")
    @PostMapping("/rollback")
    public ResponseEntity<ApiResponse<FineTuningOperationResponse>> rollback(@Valid @RequestBody RollbackRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.rollback(body), RequestIds.from(request)));
    }
}
