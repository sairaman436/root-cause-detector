/*
 * Purpose: Exposes the authenticated human-quality review workflow for the immutable held-out set.
 * Why it exists: Reviewers need a dedicated API that is distinct from training-candidate approval.
 * Architecture fit: REST adapter for the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.evaluation.application.HumanEvaluationService;
import com.airural.platform.core.evaluation.web.dto.HumanEvaluationDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Authenticated human semantic evaluation API. */
@RestController
@RequestMapping({"/api/v1/evaluation/human", "/evaluation/human"})
public class HumanEvaluationController {
    private final HumanEvaluationService service;

    public HumanEvaluationController(HumanEvaluationService service) { this.service = service; }

    /** Lists immutable examples with scored/remaining progress. */
    @Operation(summary = "List held-out human evaluation examples", description = "Lists BASE Qwen outputs and rubric review progress for evaluation-set-v1.0.0.")
    @GetMapping("/examples")
    public ResponseEntity<ApiResponse<HumanEvaluationExamplesResponse>> examples(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.examples(), RequestIds.from(request)));
    }

    /** Gets one immutable example for scoring. */
    @Operation(summary = "Get one human evaluation example", description = "Returns the held-out input, evidence, citations, and BASE Qwen output.")
    @GetMapping("/examples/{exampleId}")
    public ResponseEntity<ApiResponse<HumanEvaluationExampleResponse>> example(@PathVariable String exampleId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.example(exampleId), RequestIds.from(request)));
    }

    /** Records one explicit score set with reviewer identity derived from JWT. */
    @Operation(summary = "Submit human quality scores", description = "Persists one authenticated HUMAN-QUALITY-RUBRIC@1.0.0 review; reviewer identity is never accepted from the request body.")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<HumanEvaluationReviewResponse>> submit(
            @Valid @RequestBody HumanEvaluationReviewRequest body,
            @AuthenticationPrincipal AuthenticatedUser reviewer,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.submit(body, reviewer), RequestIds.from(request)));
    }
}

