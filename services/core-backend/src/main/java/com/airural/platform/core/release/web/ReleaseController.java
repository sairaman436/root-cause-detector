/*
 * Purpose: Exposes AI-10 release engineering APIs.
 * Why it exists: Operators need controlled access to latest release, history, artifacts, model cards, promotion, and rollback.
 * Architecture fit: REST adapter for release engineering under `/api/v1/release` and `/release`.
 */
package com.airural.platform.core.release.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.release.application.ReleaseEngineeringService;
import com.airural.platform.core.release.web.dto.ReleaseDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for enterprise AI release engineering. */
@RestController
@RequestMapping({"/api/v1/release", "/release"})
public class ReleaseController {
    private final ReleaseEngineeringService service;

    public ReleaseController(ReleaseEngineeringService service) {
        this.service = service;
    }

    /** Returns the latest stable release. */
    @Operation(summary = "Get latest model release", description = "Returns the latest stable Rural Intelligence Foundation Model release.")
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ReleaseVersionResponse>> latest(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.latest(), RequestIds.from(request)));
    }

    /** Returns release history. */
    @Operation(summary = "Get release history", description = "Returns model release history across stable, hotfix, LTS, deprecated, and retired releases.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<ReleaseHistoryResponse>> history(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.history(pageable), RequestIds.from(request)));
    }

    /** Returns release artifacts. */
    @Operation(summary = "Get release artifacts", description = "Returns production, research, development, LTS, GGUF, safetensors, Ollama, vLLM, Docker, checksum, signature, and compatibility metadata.")
    @GetMapping("/artifacts")
    public ResponseEntity<ApiResponse<ReleaseArtifactsResponse>> artifacts(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.artifacts(), RequestIds.from(request)));
    }

    /** Returns model card. */
    @Operation(summary = "Get model card", description = "Returns the generated model card for the latest stable release.")
    @GetMapping("/model-card")
    public ResponseEntity<ApiResponse<ModelCardResponse>> modelCard(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.modelCard(), RequestIds.from(request)));
    }

    /** Promotes a release. */
    @Operation(summary = "Promote release", description = "Promotes a certified and board-approved release candidate.")
    @PostMapping("/promote")
    public ResponseEntity<ApiResponse<ReleaseDecisionResponse>> promote(@Valid @RequestBody ReleaseDecisionRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.promote(body, userId(user)), RequestIds.from(request)));
    }

    /** Rolls back a release. */
    @Operation(summary = "Rollback release", description = "Rolls back, deprecates, or retires a release with release-audit evidence.")
    @PostMapping("/rollback")
    public ResponseEntity<ApiResponse<ReleaseDecisionResponse>> rollback(@Valid @RequestBody ReleaseDecisionRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.rollback(body, userId(user)), RequestIds.from(request)));
    }

    private java.util.UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
