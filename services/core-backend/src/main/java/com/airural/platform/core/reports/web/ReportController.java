/*
 * Purpose: Exposes report generation and export APIs.
 * Why it exists: Sprint 1 users need testable PDF, CSV, executive, village, and district reporting endpoints.
 * Architecture fit: REST adapter for the Reports bounded context.
 */
package com.airural.platform.core.reports.web;

import com.airural.platform.core.common.*;
import com.airural.platform.core.identity.security.AuthenticatedUser;
import com.airural.platform.core.reports.application.ReportGenerationService;
import com.airural.platform.core.reports.web.dto.ReportDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST controller for generated reports. */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportGenerationService service;

    public ReportController(ReportGenerationService service) {
        this.service = service;
    }

    @Operation(summary = "Generate report", description = "Generates a durable executive, village, or district report from a completed decision.")
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> generate(@Valid @RequestBody GenerateReportRequest body, @AuthenticationPrincipal AuthenticatedUser user, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.generate(body, userId(user)), RequestIds.from(request)));
    }

    @Operation(summary = "List reports", description = "Lists generated reports.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> reports(Pageable pageable, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.reports(pageable), RequestIds.from(request)));
    }

    @Operation(summary = "Get report", description = "Gets one generated report.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> report(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.report(id), RequestIds.from(request)));
    }

    @Operation(summary = "List decision reports", description = "Lists reports generated from one decision.")
    @GetMapping("/decision/{decisionId}")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> byDecision(@PathVariable UUID decisionId, HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.byDecision(decisionId), RequestIds.from(request)));
    }

    @Operation(summary = "Download PDF", description = "Downloads a generated report as a PDF document.")
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + id + ".pdf\"")
                .body(service.pdf(id));
    }

    @Operation(summary = "Download CSV", description = "Downloads generated report findings as CSV.")
    @GetMapping(value = "/{id}/csv", produces = "text/csv")
    public ResponseEntity<byte[]> csv(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + id + ".csv\"")
                .body(service.csv(id));
    }

    private UUID userId(AuthenticatedUser user) {
        return user == null ? null : user.userId();
    }
}
