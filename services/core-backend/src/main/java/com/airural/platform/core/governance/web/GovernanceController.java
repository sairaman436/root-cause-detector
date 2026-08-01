/*
 * Purpose: Exposes Enterprise AI Governance Platform APIs.
 * Why it exists: Governance boards and platform services require controlled policy, audit, compliance, risk, approval, and rejection endpoints.
 * Architecture fit: REST adapter for AI-9 governance under `/api/v1/governance` and `/governance`.
 */
package com.airural.platform.core.governance.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.governance.application.GovernanceService;
import com.airural.platform.core.governance.web.dto.GovernanceDtos.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for enterprise AI governance. */
@RestController
@RequestMapping({"/api/v1/governance", "/governance"})
public class GovernanceController {
    private final GovernanceService service;

    public GovernanceController(GovernanceService service) {
        this.service = service;
    }

    /** Lists governance policies. */
    @Operation(summary = "List governance policies", description = "Lists active and historical enterprise AI governance policies with rules.")
    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<Page<GovernancePolicyResponse>>> policies(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.policies(pageable), RequestIds.from(request)));
    }

    /** Creates a governance policy. */
    @Operation(summary = "Create governance policy", description = "Creates a configurable enterprise AI governance policy and rules.")
    @PostMapping("/policies")
    public ResponseEntity<ApiResponse<GovernancePolicyResponse>> createPolicy(@Valid @RequestBody GovernancePolicyRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createPolicy(body, userId(user)), RequestIds.from(request)));
    }

    /** Lists immutable audit records. */
    @Operation(summary = "List governance audit records", description = "Lists tamper-evident AI governance audit records.")
    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<Page<AuditRecordResponse>>> audit(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.audit(pageable), RequestIds.from(request)));
    }

    /** Returns compliance matrix. */
    @Operation(summary = "Get compliance matrix", description = "Returns ISO 27001, ISO 42001, NIST AI RMF, OWASP, GDPR-ready, and public sector control evidence.")
    @GetMapping("/compliance")
    public ResponseEntity<ApiResponse<ComplianceResponse>> compliance(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.compliance(), RequestIds.from(request)));
    }

    /** Returns governance risks. */
    @Operation(summary = "Get governance risks", description = "Returns model, prompt, dataset, knowledge, security, operational, compliance, and bias risks.")
    @GetMapping("/risks")
    public ResponseEntity<ApiResponse<RiskResponse>> risks(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.risks(), RequestIds.from(request)));
    }

    /** Approves a governed artifact. */
    @Operation(summary = "Approve governed artifact", description = "Approves a dataset, model, prompt, agent, knowledge source, inference flow, continuous learning artifact, deployment, or policy.")
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<GovernanceDecisionResponse>> approve(@Valid @RequestBody GovernanceDecisionRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(body, userId(user)), RequestIds.from(request)));
    }

    /** Rejects a governed artifact. */
    @Operation(summary = "Reject governed artifact", description = "Rejects a governed AI artifact and records immutable audit evidence.")
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<GovernanceDecisionResponse>> reject(@Valid @RequestBody GovernanceDecisionRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(body, userId(user)), RequestIds.from(request)));
    }

    private java.util.UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
