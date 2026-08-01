/*
 * Purpose: Exposes enterprise model optimization and deployment packaging APIs.
 * Why it exists: AI-6 users need controlled endpoints for optimization jobs, artifacts, benchmarks, packages, and release promotion.
 * Architecture fit: REST adapter for optimization platform capabilities with versioned and compatibility paths.
 */
package com.airural.platform.core.optimization.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.optimization.application.OptimizationPlatformService;
import com.airural.platform.core.optimization.web.dto.OptimizationDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for AI-6 optimization and packaging. */
@RestController
@RequestMapping({"/api/v1/optimization", "/optimization"})
public class OptimizationController {
    private final OptimizationPlatformService service;

    public OptimizationController(OptimizationPlatformService service) {
        this.service = service;
    }

    /** Starts optimization and packaging for an evaluation-approved model. */
    @Operation(summary = "Start optimization", description = "Creates optimized artifact, package, benchmark, compatibility, signature, and release candidate records without retraining or deployment.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<OptimizationRunResponse>> start(@Valid @RequestBody StartOptimizationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.start(body), RequestIds.from(request)));
    }

    /** Lists optimization jobs. */
    @Operation(summary = "List optimization jobs", description = "Lists optimization run history.")
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Page<OptimizationRunResponse>>> jobs(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.jobs(pageable), RequestIds.from(request)));
    }

    /** Lists artifacts. */
    @Operation(summary = "List optimization artifacts", description = "Lists exported, quantized, signed, and validated artifact records.")
    @GetMapping("/artifacts")
    public ResponseEntity<ApiResponse<Page<ArtifactResponse>>> artifacts(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.artifacts(pageable), RequestIds.from(request)));
    }

    /** Returns benchmark summary. */
    @Operation(summary = "Get optimization benchmarks", description = "Returns performance benchmark summary for an optimization run.")
    @GetMapping("/benchmarks")
    public ResponseEntity<ApiResponse<BenchmarkSummaryResponse>> benchmarks(@RequestParam UUID optimizationRunId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.benchmarks(optimizationRunId), RequestIds.from(request)));
    }

    /** Lists deployment packages. */
    @Operation(summary = "List deployment packages", description = "Lists Ollama, vLLM, llama.cpp, Docker, Kubernetes, offline, and enterprise packages.")
    @GetMapping("/packages")
    public ResponseEntity<ApiResponse<Page<PackageResponse>>> packages(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.packages(pageable), RequestIds.from(request)));
    }

    /** Promotes a release candidate recommendation without deploying it. */
    @Operation(summary = "Promote optimized release candidate", description = "Records release promotion after validation; no deployment is performed.")
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<OptimizationDecisionResponse>> promote(@Valid @RequestBody PromoteOptimizationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.promote(body), RequestIds.from(request)));
    }
}
