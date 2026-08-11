/*
 * Purpose: Exposes enterprise continuous learning APIs.
 * Why it exists: Governed users need endpoints for feedback, review, candidates, history, metrics, promotion, and rejection.
 * Architecture fit: REST adapter for AI-7 with versioned and compatibility paths.
 */
package com.airural.platform.core.learning.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.learning.application.ContinuousLearningService;
import com.airural.platform.core.learning.application.EvaluationTrainingCandidateService;
import com.airural.platform.core.learning.web.dto.LearningDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for continuous learning. */
@RestController
@RequestMapping({"/api/v1/learning", "/learning"})
public class LearningController {
    private final ContinuousLearningService service;
    private final EvaluationTrainingCandidateService evaluationCandidateService;

    public LearningController(ContinuousLearningService service, EvaluationTrainingCandidateService evaluationCandidateService) {
        this.service = service;
        this.evaluationCandidateService = evaluationCandidateService;
    }

    /** Captures feedback. */
    @Operation(summary = "Capture learning feedback", description = "Captures operational feedback and creates a governed learning record without retraining.")
    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<LearningRecordResponse>> feedback(@Valid @RequestBody FeedbackRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.feedback(body), RequestIds.from(request)));
    }

    /** Reviews feedback. */
    @Operation(summary = "Review learning record", description = "Records human review and optionally queues a training candidate for governance approval.")
    @PostMapping("/review")
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> review(@Valid @RequestBody ReviewRequest body, @AuthenticationPrincipal AuthenticatedUser reviewer, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.review(body, reviewer), RequestIds.from(request)));
    }

    /** Lists training candidates. */
    @Operation(summary = "List learning candidates", description = "Lists future training dataset candidates.")
    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<Page<TrainingCandidateResponse>>> candidates(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.candidates(pageable), RequestIds.from(request)));
    }

    /** Reviews one training candidate using the authenticated reviewer identity. */
    @Operation(summary = "Review training candidate", description = "Approves, corrects, or rejects a real training candidate. Reviewer identity is derived from the JWT.")
    @PostMapping("/candidates/{candidateId}/review")
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> reviewCandidate(@PathVariable UUID candidateId, @Valid @RequestBody CandidateReviewRequest body, @AuthenticationPrincipal AuthenticatedUser reviewer, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reviewCandidate(candidateId, body, reviewer), RequestIds.from(request)));
    }

    /** Queues eligible evaluation results for the existing human-review workflow. */
    @Operation(summary = "Generate training candidates from evaluation results", description = "Queues only passed, non-synthetic, quality-gated evaluation examples; no candidate is approved automatically.")
    @PostMapping("/candidates/generate-from-evaluations")
    public ResponseEntity<ApiResponse<CandidateGenerationResponse>> generateFromEvaluations(@RequestBody(required = false) CandidateGenerationRequest body, @AuthenticationPrincipal AuthenticatedUser actor, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(evaluationCandidateService.generate(body, actor), RequestIds.from(request)));
    }

    /** Queues a proposed correction without changing the approved source candidate. */
    @Operation(summary = "Queue governed correction proposal", description = "Copies an approved source candidate into a pending v0.2 correction candidate, preserving the original output, proposed correction, provenance, and authenticated proposer. A separate human review is still required.")
    @PostMapping("/candidates/correction-proposals")
    public ResponseEntity<ApiResponse<TrainingCandidateResponse>> correctionProposal(@Valid @RequestBody CorrectionProposalRequest body, @AuthenticationPrincipal AuthenticatedUser proposer, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createCorrectionProposal(body, proposer), RequestIds.from(request)));
    }

    /** Exports only approved real candidates to the existing dataset validation boundary. */
    @Operation(summary = "Export approved training candidates", description = "Returns dataset-v0.1-compatible candidates after authenticated human approval; pending, rejected, duplicate, PII, unsupported, and synthetic records are excluded.")
    @GetMapping("/dataset-export")
    public ResponseEntity<ApiResponse<DatasetExportResponse>> datasetExport(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.datasetExport(), RequestIds.from(request)));
    }

    /** Lists learning history. */
    @Operation(summary = "List learning history", description = "Lists governed learning records.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<LearningRecordResponse>>> history(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.history(pageable), RequestIds.from(request)));
    }

    /** Returns learning metrics. */
    @Operation(summary = "Get learning metrics", description = "Returns continuous learning observability metrics.")
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<LearningMetricsResponse>> metrics(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.metrics(), RequestIds.from(request)));
    }

    /** Promotes a candidate for future dataset inclusion. */
    @Operation(summary = "Promote learning candidate", description = "Approves a candidate for future training dataset preparation only.")
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> promote(@Valid @RequestBody PromoteLearningRequest body, @AuthenticationPrincipal AuthenticatedUser reviewer, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.promote(body, reviewer), RequestIds.from(request)));
    }

    /** Rejects a candidate. */
    @Operation(summary = "Reject learning candidate", description = "Rejects a candidate from future training datasets.")
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> reject(@Valid @RequestBody RejectLearningRequest body, @AuthenticationPrincipal AuthenticatedUser reviewer, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(body, reviewer), RequestIds.from(request)));
    }
}
