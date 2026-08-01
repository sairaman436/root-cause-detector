/*
 * Purpose: Exposes enterprise AI evaluation APIs.
 * Why it exists: Independent evaluation teams need controlled endpoints for running evaluations, reading results, safety, comparisons, promotion recommendations, and rejection records.
 * Architecture fit: REST adapter for AI-5 with platform-versioned and requested compatibility paths.
 */
package com.airural.platform.core.evaluation.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.evaluation.application.EvaluationPlatformService;
import com.airural.platform.core.evaluation.web.dto.EvaluationDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for enterprise AI evaluation. */
@RestController
@RequestMapping({"/api/v1/evaluation", "/evaluation"})
public class EvaluationController {
    private final EvaluationPlatformService service;

    public EvaluationController(EvaluationPlatformService service) {
        this.service = service;
    }

    /** Runs immutable model evaluation. */
    @Operation(summary = "Run evaluation", description = "Runs immutable benchmark, safety, red-team, citation, hallucination, comparison, and approval workflows without retraining or deployment.")
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<EvaluationRunResponse>> run(@Valid @RequestBody RunEvaluationRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.run(body), RequestIds.from(request)));
    }

    /** Lists evaluation results. */
    @Operation(summary = "List evaluation results", description = "Lists immutable evaluation run results.")
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<Page<EvaluationRunResponse>>> results(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.results(pageable), RequestIds.from(request)));
    }

    /** Lists benchmark suites. */
    @Operation(summary = "List benchmarks", description = "Lists registered benchmark suites.")
    @GetMapping("/benchmarks")
    public ResponseEntity<ApiResponse<Page<BenchmarkSuiteResponse>>> benchmarks(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.benchmarks(pageable), RequestIds.from(request)));
    }

    /** Returns safety summary. */
    @Operation(summary = "Get safety summary", description = "Returns safety framework summary for an evaluation run.")
    @GetMapping("/safety")
    public ResponseEntity<ApiResponse<SafetySummaryResponse>> safety(@RequestParam UUID evaluationRunId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.safety(evaluationRunId), RequestIds.from(request)));
    }

    /** Returns model comparison summary. */
    @Operation(summary = "Get model comparison", description = "Returns comparison recommendation against baseline models.")
    @GetMapping("/comparison")
    public ResponseEntity<ApiResponse<ComparisonResponse>> comparison(@RequestParam UUID evaluationRunId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.comparison(evaluationRunId), RequestIds.from(request)));
    }

    /** Records promotion recommendation. */
    @Operation(summary = "Promote model recommendation", description = "Records promotion recommendation only; no deployment or model merge is performed.")
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<EvaluationDecisionResponse>> promote(@Valid @RequestBody PromotionRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.promote(body), RequestIds.from(request)));
    }

    /** Records rejection decision. */
    @Operation(summary = "Reject model", description = "Records rejection decision only; no retraining or deployment is performed.")
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<EvaluationDecisionResponse>> reject(@Valid @RequestBody RejectionRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(body), RequestIds.from(request)));
    }
}
