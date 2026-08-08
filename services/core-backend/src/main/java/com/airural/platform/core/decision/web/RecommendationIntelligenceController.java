/*
 * Purpose: Exposes recommendation intelligence APIs.
 * Why it exists: Validated root causes need a secured API surface for intervention options, evidence, risks, review, approval, rejection, and regeneration.
 * Architecture fit: REST adapter for the Decision Intelligence bounded context.
 */
package com.airural.platform.core.decision.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.decision.application.RecommendationIntelligenceService;
import com.airural.platform.core.decision.web.dto.RecommendationIntelligenceDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for human-approved recommendation decision support. */
@RestController
@RequestMapping({"/api/v1/recommendations", "/recommendations"})
public class RecommendationIntelligenceController {
    private final RecommendationIntelligenceService service;

    public RecommendationIntelligenceController(RecommendationIntelligenceService service) {
        this.service = service;
    }

    /** Generates prioritized intervention options from validated root causes. */
    @Operation(summary = "Generate recommendations", description = "Generates evidence-grounded, resource-aware, human-reviewable intervention options linked to validated root causes.")
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<RecommendationSetResponse>> generate(@Valid @RequestBody RecommendationGenerateRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.generate(body, userId(user)), RequestIds.from(request)));
    }

    /** Gets one stored recommendation set. */
    @Operation(summary = "Get recommendation set", description = "Returns the persisted recommendation set with options, comparison scores, scheme matches, and generation metadata.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecommendationSetResponse>> get(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id), RequestIds.from(request)));
    }

    /** Gets generated intervention options. */
    @Operation(summary = "Get recommendation options", description = "Returns prioritized options and trade-off comparison records.")
    @GetMapping("/{id}/options")
    public ResponseEntity<ApiResponse<Map<String, Object>>> options(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.options(id), RequestIds.from(request)));
    }

    /** Gets evidence links for recommendation options. */
    @Operation(summary = "Get recommendation evidence", description = "Returns evidence links separated by evidence-backed, retrieved-knowledge, model-generated, and human-provided sources.")
    @GetMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evidence(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.evidence(id), RequestIds.from(request)));
    }

    /** Gets recommendation risks. */
    @Operation(summary = "Get recommendation risks", description = "Returns risks, assumptions, severity, likelihood, and mitigations for generated recommendation options.")
    @GetMapping("/{id}/risks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> risks(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.risks(id), RequestIds.from(request)));
    }

    /** Records a human review action without executing any intervention. */
    @Operation(summary = "Review recommendations", description = "Records accept, edit, reject, request-more-evidence, or approve review decisions without automatic intervention execution.")
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<RecommendationReviewResponse>> review(@PathVariable UUID id, @Valid @RequestBody RecommendationReviewRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.review(id, body, userId(user)), RequestIds.from(request)));
    }

    /** Approves a recommendation set for human-led implementation planning. */
    @Operation(summary = "Approve recommendations", description = "Marks a recommendation set approved by a human reviewer; the platform does not execute the intervention.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RecommendationReviewResponse>> approve(@PathVariable UUID id, @Valid @RequestBody RecommendationReviewRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id, body, userId(user)), RequestIds.from(request)));
    }

    /** Rejects a recommendation set. */
    @Operation(summary = "Reject recommendations", description = "Records human rejection and preserves the generated recommendation set for audit.")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RecommendationReviewResponse>> reject(@PathVariable UUID id, @Valid @RequestBody RecommendationReviewRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(id, body, userId(user)), RequestIds.from(request)));
    }

    /** Regenerates recommendations from a prior recommendation set. */
    @Operation(summary = "Regenerate recommendations", description = "Supersedes the previous set and creates a new versioned recommendation set from the prior context.")
    @PostMapping("/{id}/regenerate")
    public ResponseEntity<ApiResponse<RecommendationSetResponse>> regenerate(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.regenerate(id, userId(user)), RequestIds.from(request)));
    }

    private UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
