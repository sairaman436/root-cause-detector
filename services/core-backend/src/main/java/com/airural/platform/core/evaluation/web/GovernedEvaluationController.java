/*
 * Purpose: Exposes the single controlled development evaluation operation.
 * Why it exists: Evaluation operators need a secured application entry point that creates
 * a persisted pilot result without touching training data or synthetic review fixtures.
 * Architecture fit: REST adapter for the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.evaluation.application.GovernedEvaluationScenarioService;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.GovernedEvaluationResponse;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.GovernedEvaluationBatchResponse;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Secured entry point for the first governed development evaluation. */
@RestController
@RequestMapping({"/api/v1/evaluation/pilot", "/evaluation/pilot"})
public class GovernedEvaluationController {
    private final GovernedEvaluationScenarioService service;

    public GovernedEvaluationController(GovernedEvaluationScenarioService service) {
        this.service = service;
    }

    /** Runs exactly one labelled synthetic development scenario through the full pipeline. */
    @Operation(summary = "Run first governed development evaluation", description = "Executes one synthetic, development-only survey/evidence/RAG/Qwen/root-cause/recommendation evaluation and persists its pilot result. It never promotes data into training.")
    @PostMapping("/first-development-scenario")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> run(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.run(userId), RequestIds.from(request)));
    }

    /** Runs the fixed controlled pilot set; every result remains pending human review. */
    @Operation(summary = "Run controlled pilot evaluation set", description = "Executes three constructed PILOT_EVALUATION scenarios through RAG, Qwen, root-cause, recommendation, and evaluation persistence. No result is approved or promoted automatically.")
    @PostMapping("/pilot-evaluation-batch")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotBatch(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotBatch(userId), RequestIds.from(request)));
    }

    /** Runs only the additional labelled scenarios needed to expand dataset coverage. */
    @Operation(summary = "Run governed pilot corpus expansion", description = "Executes additional PILOT_EVALUATION scenarios for root-cause, recommendation, and RAG-grounded task coverage. Every result remains pending authenticated human review and no dataset is modified.")
    @PostMapping("/pilot-evaluation-expansion")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotExpansion(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotExpansion(userId), RequestIds.from(request)));
    }

    /** Runs uniquely keyed v0.2 preparation scenarios and leaves all results pending review. */
    @Operation(summary = "Run dataset v0.2 pilot preparation", description = "Executes six additional PILOT_EVALUATION scenarios across root-cause, recommendation, and RAG tasks. Results remain pending human review and no dataset is modified.")
    @PostMapping("/pilot-evaluation-v02-preparation")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotV02Preparation(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV02Preparation(userId), RequestIds.from(request)));
    }

    /** Runs the v0.3 contract-focused pilot corpus; every result remains pending review. */
    @Operation(summary = "Run dataset v0.3 pilot preparation", description = "Executes nine new PILOT_EVALUATION scenarios with canonical root-cause, recommendation, and RAG output contracts. Results remain pending authenticated human review and no dataset is modified.")
    @PostMapping("/pilot-evaluation-v03-preparation")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotV03Preparation(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV03Preparation(userId), RequestIds.from(request)));
    }

    /** Re-runs one server-owned pilot scenario without deleting its prior evaluation result. */
    @Operation(summary = "Re-run pilot evaluation", description = "Re-runs one existing PILOT_EVALUATION scenario after a verified pipeline-quality fix. The original run remains immutable and the new result remains pending human review.")
    @PostMapping("/{scenarioKey}/rerun")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> rerun(@PathVariable String scenarioKey, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.rerunPilotScenario(userId, scenarioKey), RequestIds.from(request)));
    }
}
