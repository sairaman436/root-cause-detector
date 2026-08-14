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
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.RecommendationCoverageBatchResponse;
import com.airural.platform.core.evaluation.web.dto.GovernedEvaluationDtos.RecommendationCoverageCandidateResponse;
import com.airural.platform.core.evaluation.application.RecommendationCoverageService;
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
    private final RecommendationCoverageService recommendationCoverageService;

    public GovernedEvaluationController(GovernedEvaluationScenarioService service, RecommendationCoverageService recommendationCoverageService) {
        this.service = service;
        this.recommendationCoverageService = recommendationCoverageService;
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

    /** Runs uniquely keyed v0.3 expansion scenarios without approving or materializing data. */
    @Operation(summary = "Run dataset v0.3 pilot expansion", description = "Executes additional PILOT_EVALUATION scenarios with bounded canonical outputs. Every result remains pending authenticated human review and no dataset is modified.")
    @PostMapping("/pilot-evaluation-v03-expansion")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotV03Expansion(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV03Expansion(userId), RequestIds.from(request)));
    }

    /** Runs the v0.5 domain-diversity candidate set; every result remains pending review. */
    @Operation(summary = "Run dataset v0.5 domain diversity", description = "Executes 24 distinct PILOT_EVALUATION scenarios across eight rural domains and three task types. Every result remains pending authenticated human review and no dataset is modified.")
    @PostMapping("/pilot-evaluation-v05-diversity")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotV05Diversity(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV05Diversity(userId), RequestIds.from(request)));
    }

    /** Runs one v0.5 diversity scenario independently for provider or evidence diagnosis. */
    @Operation(summary = "Run one v0.5 diversity scenario", description = "Executes one registered PILOT_EVALUATION diversity scenario through the existing evidence, RAG, Qwen, root-cause, recommendation, and evaluation pipeline. It never approves or promotes a candidate.")
    @PostMapping("/pilot-evaluation-v05-diversity/{scenarioKey}")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> runPilotV05DiversityScenario(@PathVariable String scenarioKey, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV05DiversityScenario(userId, scenarioKey), RequestIds.from(request)));
    }

    /** Runs v0.5 correction and replacement versions without mutating prior candidates. */
    @Operation(summary = "Run v0.5 quality remediation", description = "Creates new immutable correction/replacement PILOT_EVALUATION results with scenario-isolated evidence. Existing candidates remain unchanged and all new candidates require human review.")
    @PostMapping("/pilot-evaluation-v05-quality-remediation")
    public ResponseEntity<ApiResponse<GovernedEvaluationBatchResponse>> runPilotV05QualityRemediation(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV05QualityRemediation(userId), RequestIds.from(request)));
    }

    /** Runs one v0.5 correction or replacement version independently. */
    @Operation(summary = "Run one v0.5 quality remediation scenario", description = "Runs one new correction or replacement scenario through isolated evidence retrieval and the existing governed pipeline. It never approves or promotes a candidate.")
    @PostMapping("/pilot-evaluation-v05-quality-remediation/{scenarioKey}")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> runPilotV05QualityRemediationScenario(@PathVariable String scenarioKey, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV05QualityRemediationScenario(userId, scenarioKey), RequestIds.from(request)));
    }

    /** Runs one uniquely keyed v0.3 expansion scenario; no review decision or dataset promotion occurs. */
    @Operation(summary = "Run one v0.3 pilot expansion scenario", description = "Executes one registered PILOT_EVALUATION scenario through the existing RAG, Qwen, root-cause, recommendation, and evaluation pipeline. The result remains pending human review.")
    @PostMapping("/pilot-evaluation-v03-expansion/{scenarioKey}")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> runPilotV03ExpansionScenario(@PathVariable String scenarioKey, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV03ExpansionScenario(userId, scenarioKey), RequestIds.from(request)));
    }

    /** Runs one Experiment 003 scenario and leaves the evaluation pending human review. */
    @Operation(summary = "Run one Experiment 003 pilot scenario", description = "Executes a new balanced PILOT_EVALUATION scenario through the existing RAG, Qwen, root-cause, recommendation, and evaluation pipeline. It never approves or promotes a candidate.")
    @PostMapping("/pilot-evaluation-v03-experiment-003/{scenarioKey}")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> runPilotV03Experiment003Scenario(@PathVariable String scenarioKey, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.runPilotV03Experiment003Scenario(userId, scenarioKey), RequestIds.from(request)));
    }

    /** Generates recommendation evaluations only from approved, non-synthetic v0.5 root causes. */
    @Operation(summary = "Generate recommendation coverage from approved roots", description = "Derives recommendation-only PILOT_EVALUATION results from explicitly human-approved root-cause candidates. Results remain pending review and no dataset is modified.")
    @PostMapping("/pilot-evaluation-v05-recommendation-coverage")
    public ResponseEntity<ApiResponse<RecommendationCoverageBatchResponse>> runRecommendationCoverage(@AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(recommendationCoverageService.generateAll(userId), RequestIds.from(request)));
    }

    /** Generates one recommendation evaluation from a selected approved root candidate. */
    @Operation(summary = "Generate one recommendation coverage result", description = "Uses one explicitly approved root-cause training candidate as the governed recommendation source. The source candidate is never changed or approved by this operation.")
    @PostMapping("/pilot-evaluation-v05-recommendation-coverage/{candidateId}")
    public ResponseEntity<ApiResponse<RecommendationCoverageCandidateResponse>> runRecommendationCoverageCandidate(@PathVariable UUID candidateId, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(recommendationCoverageService.generateOne(candidateId, userId), RequestIds.from(request)));
    }

    /** Re-runs one server-owned pilot scenario without deleting its prior evaluation result. */
    @Operation(summary = "Re-run pilot evaluation", description = "Re-runs one existing PILOT_EVALUATION scenario after a verified pipeline-quality fix. The original run remains immutable and the new result remains pending human review.")
    @PostMapping("/{scenarioKey}/rerun")
    public ResponseEntity<ApiResponse<GovernedEvaluationResponse>> rerun(@PathVariable String scenarioKey, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        UUID userId = user == null ? null : user.userId();
        return ResponseEntity.ok(ApiResponse.success(service.rerunPilotScenario(userId, scenarioKey), RequestIds.from(request)));
    }
}
