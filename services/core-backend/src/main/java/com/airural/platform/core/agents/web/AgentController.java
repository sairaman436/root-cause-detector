/*
 * Purpose: Exposes multi-agent intelligence APIs.
 * Why it exists: Users and operators need secure access to agent chat, execution, registry, tasks, memory, history, feedback, and tools.
 * Architecture fit: REST adapter for Milestone 9 Multi-Agent Intelligence.
 */
package com.airural.platform.core.agents.web;

import com.airural.platform.core.agents.application.AgentOrchestratorService;
import com.airural.platform.core.agents.web.dto.AgentDtos.*;
import com.airural.platform.core.common.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for agent operations. */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
    private final AgentOrchestratorService service;

    public AgentController(AgentOrchestratorService service) {
        this.service = service;
    }

    @Operation(summary = "Chat with agents", description = "Runs a conversational request through the central agent orchestrator.")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AgentExecutionResponse>> chat(@Valid @RequestBody AgentChatRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.chat(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "Execute agent task", description = "Plans, routes, executes, aggregates, and evaluates a multi-agent objective.")
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<AgentExecutionResponse>> execute(@Valid @RequestBody AgentExecuteRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.execute(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "List agents", description = "Lists registered specialized agents.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AgentResponse>>> agents(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.agents(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "List agent tasks", description = "Lists routed agent tasks and statuses.")
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<Page<AgentTaskResponse>>> tasks(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.tasks(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "List agent memory", description = "Lists shared, conversation, session, task, village, survey, and knowledge reference memory.")
    @GetMapping("/memory")
    public ResponseEntity<ApiResponse<List<AgentMemoryResponse>>> memory(@RequestParam(required = false) UUID conversationId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.memory(conversationId), RequestIds.from(request)));
    }

    @Operation(summary = "List agent history", description = "Lists historical agent executions.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<AgentHistoryResponse>>> history(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.history(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Submit agent feedback", description = "Records human feedback and approval decisions for agent output.")
    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<AgentFeedbackResponse>> feedback(@Valid @RequestBody AgentFeedbackRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.feedback(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "List tools", description = "Lists MCP-style tool definitions, metadata, permissions, and health.")
    @GetMapping("/tools")
    public ResponseEntity<ApiResponse<Page<ToolResponse>>> tools(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.tools(pageable), RequestIds.from(request)));
    }

    private UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
