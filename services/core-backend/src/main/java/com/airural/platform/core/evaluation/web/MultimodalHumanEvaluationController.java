/*
 * Purpose: Exposes immutable multimodal traces and authenticated human scoring.
 * Why it exists: The portal needs a server-backed review workflow separate from browser-local drafts and text evaluation.
 * Architecture fit: REST adapter for the evaluation bounded context.
 */
package com.airural.platform.core.evaluation.web;

import com.airural.platform.core.common.ApiResponse;
import com.airural.platform.core.common.RequestIds;
import com.airural.platform.core.evaluation.application.MultimodalHumanEvaluationService;
import com.airural.platform.core.evaluation.web.dto.MultimodalEvaluationDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated multimodal human evaluation API. */
@RestController
@RequestMapping({"/api/v1/evaluation/multimodal", "/evaluation/multimodal"})
public class MultimodalHumanEvaluationController {
    private final MultimodalHumanEvaluationService service;

    public MultimodalHumanEvaluationController(MultimodalHumanEvaluationService service) { this.service = service; }

    /** Lists immutable multimodal traces with review progress and server-computed summaries. */
    @Operation(summary = "List multimodal evaluation traces")
    @GetMapping("/traces")
    public ResponseEntity<ApiResponse<TraceQueueResponse>> traces(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.traces(), RequestIds.from(request)));
    }

    /** Returns one sanitized immutable trace. */
    @Operation(summary = "Get a multimodal evaluation trace")
    @GetMapping("/traces/{traceId}")
    public ResponseEntity<ApiResponse<TraceResponse>> trace(@PathVariable String traceId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.trace(traceId), RequestIds.from(request)));
    }

    /** Persists one explicit human review using reviewer identity from JWT. */
    @Operation(summary = "Submit multimodal human evaluation")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> submit(@Valid @RequestBody ReviewRequest body,
            @AuthenticationPrincipal AuthenticatedUser reviewer, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.submit(body, reviewer), RequestIds.from(request)));
    }
}

