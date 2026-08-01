/*
 * Purpose: Exposes decision intelligence APIs.
 * Why it exists: Users need secured root-cause, recommendation, history, explanation, and confidence endpoints.
 * Architecture fit: REST adapter for Milestone 10 Decision Intelligence.
 */
package com.airural.platform.core.decision.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.decision.application.DecisionIntelligenceService;
import com.airural.platform.core.decision.web.dto.DecisionDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for decision intelligence. */
@RestController
@RequestMapping("/api/v1/decision")
public class DecisionController {
    private final DecisionIntelligenceService service;

    public DecisionController(DecisionIntelligenceService service) {
        this.service = service;
    }

    @Operation(summary = "Analyze decision", description = "Runs the full evidence fusion, rule, hypothesis, confidence, recommendation, and trace pipeline.")
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<DecisionResponse>> analyze(@Valid @RequestBody DecisionAnalyzeRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.analyze(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "Discover root cause", description = "Runs the structured root-cause discovery pipeline.")
    @PostMapping("/root-cause")
    public ResponseEntity<ApiResponse<DecisionResponse>> rootCause(@Valid @RequestBody RootCauseRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.rootCause(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "Generate recommendations", description = "Generates policy-aware, confidence-scored, reviewable recommendations.")
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<DecisionResponse>> recommend(@Valid @RequestBody RecommendationRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.recommend(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "List decision history", description = "Lists historical decisions and recommendation memory.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<DecisionResponse>>> history(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.history(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Get decision", description = "Gets a decision by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DecisionResponse>> decision(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.decision(id), RequestIds.from(request)));
    }

    @Operation(summary = "Get decision explanation", description = "Gets reasoning traces, hypotheses, root causes, recommendations, and citations.")
    @GetMapping("/explanation/{id}")
    public ResponseEntity<ApiResponse<ExplanationResponse>> explanation(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.explanation(id), RequestIds.from(request)));
    }

    @Operation(summary = "Get confidence details", description = "Gets confidence components, reason codes, missing evidence, and follow-ups.")
    @GetMapping("/confidence/{id}")
    public ResponseEntity<ApiResponse<ConfidenceResponse>> confidence(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.confidence(id), RequestIds.from(request)));
    }

    private UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
