/*
 * Purpose: Defines REST contracts for generated reports.
 * Why it exists: Clients need stable request and response shapes for PDF, CSV, executive, village, and district reports.
 * Architecture fit: Web adapter DTOs for the Reports bounded context.
 */
package com.airural.platform.core.reports.web.dto;

import com.airural.platform.core.reports.domain.ReportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** Namespace for report DTOs. */
public final class ReportDtos {
    private ReportDtos() {}

    /** Request to generate a report from a completed decision. */
    public record GenerateReportRequest(
            @NotNull UUID decisionId,
            UUID surveyId,
            UUID organizationId,
            @NotNull ReportType reportType,
            @Size(max = 220) String title) {}

    /** Generated report metadata and content response. */
    public record ReportResponse(
            UUID id,
            UUID decisionId,
            UUID surveyId,
            UUID organizationId,
            ReportType reportType,
            String title,
            String status,
            String executiveSummary,
            String contentMarkdown,
            String csvDownloadUrl,
            String pdfDownloadUrl,
            UUID generatedBy,
            Instant generatedAt) {}
}
