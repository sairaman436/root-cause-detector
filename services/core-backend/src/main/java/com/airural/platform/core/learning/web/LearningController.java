/*
 * Purpose: Exposes enterprise continuous learning APIs.
 * Why it exists: Governed users need endpoints for feedback, review, candidates, history, metrics, promotion, and rejection.
 * Architecture fit: REST adapter for AI-7 with versioned and compatibility paths.
 */
package com.airural.platform.core.learning.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.learning.application.ContinuousLearningService;
import com.airural.platform.core.learning.web.dto.LearningDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for continuous learning. */
@RestController
@RequestMapping({"/api/v1/learning", "/learning"})
public class LearningController {
    private final ContinuousLearningService service;

    public LearningController(ContinuousLearningService service) {
        this.service = service;
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
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> review(@Valid @RequestBody ReviewRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.review(body), RequestIds.from(request)));
    }

    /** Lists training candidates. */
    @Operation(summary = "List learning candidates", description = "Lists future training dataset candidates.")
    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<Page<TrainingCandidateResponse>>> candidates(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.candidates(pageable), RequestIds.from(request)));
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
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> promote(@Valid @RequestBody PromoteLearningRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.promote(body), RequestIds.from(request)));
    }

    /** Rejects a candidate. */
    @Operation(summary = "Reject learning candidate", description = "Rejects a candidate from future training datasets.")
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<LearningDecisionResponse>> reject(@Valid @RequestBody RejectLearningRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(body), RequestIds.from(request)));
    }
}
