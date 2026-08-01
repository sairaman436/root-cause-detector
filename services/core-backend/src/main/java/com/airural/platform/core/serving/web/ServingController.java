/*
 * Purpose: Exposes AI-8 enterprise serving APIs.
 * Why it exists: All platform services need a single controlled gateway for inference, streaming, models, health, metrics, and sessions.
 * Architecture fit: REST adapter for model serving endpoints under the approved `/api/v1/ai` surface.
 */
package com.airural.platform.core.serving.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.serving.application.ServingGatewayService;
import com.airural.platform.core.serving.web.dto.ServingDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for enterprise AI serving. */
@RestController
@RequestMapping({"/api/v1/ai", "/ai"})
public class ServingController {
    private final ServingGatewayService service;

    public ServingController(ServingGatewayService service) {
        this.service = service;
    }

    /** Runs unified inference. */
    @Operation(summary = "Run inference", description = "Runs authenticated, policy-validated, prompt-secured, routed, validated, audited inference.")
    @PostMapping("/inference")
    public ResponseEntity<ApiResponse<ServingInferenceResponse>> inference(@Valid @RequestBody ServingInferenceRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.infer(body, userId(user)), RequestIds.from(request)));
    }

    /** Runs streaming-compatible inference. */
    @Operation(summary = "Run streaming inference", description = "Records a streaming inference request through the same gateway pipeline; transport streaming can be enabled by deployment adapter.")
    @PostMapping("/stream")
    public ResponseEntity<ApiResponse<ServingInferenceResponse>> stream(@Valid @RequestBody ServingInferenceRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        ServingInferenceRequest streaming = new ServingInferenceRequest(body.prompt(), body.taskType(), body.assistantType(), body.language(), body.userRole(), body.sessionId(), body.context(), true, body.batch(), body.async(), body.requestSignature(), body.tenantId());
        return ResponseEntity.ok(ApiResponse.success(service.infer(streaming, userId(user)), RequestIds.from(request)));
    }

    /** Returns serving health. */
    @Operation(summary = "Get AI serving health", description = "Returns serving node and circuit-breaker health.")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<ServingHealthResponse>> health(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.health(), RequestIds.from(request)));
    }

    /** Returns serving metrics. */
    @Operation(summary = "Get AI serving metrics", description = "Returns requests/sec, latency, token throughput, error rate, and timeout rate.")
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<ServingMetricsResponse>> metrics(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.metrics(), RequestIds.from(request)));
    }

    /** Lists inference sessions. */
    @Operation(summary = "List inference sessions", description = "Lists bounded serving sessions and context lifecycle records.")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Page<ServingSessionResponse>>> sessions(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.sessions(pageable), RequestIds.from(request)));
    }

    private java.util.UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
