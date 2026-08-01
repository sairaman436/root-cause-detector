/*
 * Purpose: Exposes AI foundation APIs.
 * Why it exists: Clients and operators need controlled access to chat, embeddings, model registry, prompts, RAG, usage, and inference logs.
 * Architecture fit: REST adapter for Milestone 8 AI Foundation.
 */
package com.airural.platform.core.ai.web;

import com.airural.platform.core.ai.application.AiFoundationService;
import com.airural.platform.core.ai.web.dto.AiDtos.*;
import com.airural.platform.core.common.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.serving.application.ServingGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for AI foundation operations. */
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiFoundationService service;
    private final ServingGatewayService servingGateway;

    public AiController(AiFoundationService service, ServingGatewayService servingGateway) {
        this.service = service;
        this.servingGateway = servingGateway;
    }

    @Operation(summary = "Chat through AI gateway", description = "Routes a prompt through safety validation, model routing, accounting, and fallback-aware response generation.")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(servingGateway.chat(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "Embed text", description = "Chunks text, creates embedding metadata, and prepares vector search records.")
    @PostMapping("/embed")
    public ResponseEntity<ApiResponse<EmbedResponse>> embed(@Valid @RequestBody EmbedRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.embed(body), RequestIds.from(request)));
    }

    @Operation(summary = "List models", description = "Lists governed model registry records and version metadata.")
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<Page<ModelResponse>>> models(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.models(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Register model", description = "Registers a model and initial version for gateway routing and governance.")
    @PostMapping("/models/register")
    public ResponseEntity<ApiResponse<ModelResponse>> registerModel(@Valid @RequestBody RegisterModelRequest body, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.registerModel(body), RequestIds.from(request)));
    }

    @Operation(summary = "Create prompt", description = "Creates a prompt template and immutable first version.")
    @PostMapping("/prompts")
    public ResponseEntity<ApiResponse<PromptResponse>> createPrompt(@Valid @RequestBody PromptRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createPrompt(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "List prompts", description = "Lists prompt templates with latest versions.")
    @GetMapping("/prompts")
    public ResponseEntity<ApiResponse<Page<PromptResponse>>> prompts(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.prompts(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Run RAG query", description = "Runs query safety validation, embedding-aware retrieval, context assembly, gateway inference logging, and citation persistence.")
    @PostMapping("/rag/query")
    public ResponseEntity<ApiResponse<RagQueryResponse>> rag(@Valid @RequestBody RagQueryRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.rag(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "List AI usage", description = "Lists token usage and estimated cost records.")
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<Page<UsageResponse>>> usage(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.usage(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "List inference logs", description = "Lists AI gateway inference telemetry.")
    @GetMapping("/inference")
    public ResponseEntity<ApiResponse<Page<InferenceResponse>>> inference(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.inferences(pageable), RequestIds.from(request)));
    }

    private UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
