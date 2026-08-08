/*
 * Purpose: Exposes root-cause intelligence APIs.
 * Why it exists: Clients need a dedicated analysis surface for problem representation, evidence review, causal graph, regeneration, and human validation.
 * Architecture fit: REST adapter for the Decision Intelligence bounded context.
 */
package com.airural.platform.core.decision.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.decision.application.RootCauseIntelligenceService;
import com.airural.platform.core.decision.web.dto.RootCauseDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for transparent root-cause intelligence analysis. */
@RestController
@RequestMapping({"/api/v1/analysis/root-cause", "/analysis/root-cause"})
public class RootCauseAnalysisController {
    private final RootCauseIntelligenceService service;

    public RootCauseAnalysisController(RootCauseIntelligenceService service) {
        this.service = service;
    }

    /** Generates a new structured root-cause analysis. */
    @Operation(summary = "Generate root-cause analysis", description = "Fuses survey data, uploaded evidence, structured data, and RAG knowledge into fact-separated root-cause intelligence.")
    @PostMapping
    public ResponseEntity<ApiResponse<RootCauseAnalysisResponse>> analyze(@Valid @RequestBody RootCauseAnalysisRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.analyze(body, userId(user)), RequestIds.from(request)));
    }

    /** Gets a stored root-cause analysis. */
    @Operation(summary = "Get root-cause analysis", description = "Returns the persisted structured analysis JSON.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RootCauseAnalysisResponse>> get(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id), RequestIds.from(request)));
    }

    /** Gets evidence and fact records for an analysis. */
    @Operation(summary = "Get root-cause evidence", description = "Returns observed facts, retrieved evidence, and evidence scoring records.")
    @GetMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evidence(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.evidence(id), RequestIds.from(request)));
    }

    /** Gets causal graph nodes and edges. */
    @Operation(summary = "Get causal graph", description = "Returns candidate cause, factor, and outcome relationships with confidence and evidence metadata.")
    @GetMapping("/{id}/causal-graph")
    public ResponseEntity<ApiResponse<Map<String, Object>>> causalGraph(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.causalGraph(id), RequestIds.from(request)));
    }

    /** Records human validation for an analysis. */
    @Operation(summary = "Review root-cause analysis", description = "Records accept, reject, modify, add-evidence, or incorrect-reasoning review decisions without overwriting the generated analysis.")
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<HumanReviewResponse>> review(@PathVariable UUID id, @Valid @RequestBody HumanReviewRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.review(id, body, userId(user)), RequestIds.from(request)));
    }

    /** Regenerates a new version from prior problem context. */
    @Operation(summary = "Regenerate root-cause analysis", description = "Supersedes the prior analysis and creates a new analysis version without deleting review history.")
    @PostMapping("/{id}/regenerate")
    public ResponseEntity<ApiResponse<RootCauseAnalysisResponse>> regenerate(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.regenerate(id, userId(user)), RequestIds.from(request)));
    }

    private UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
